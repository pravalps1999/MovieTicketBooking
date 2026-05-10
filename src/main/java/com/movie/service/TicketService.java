package com.movie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.Repository.ShowRepository;
import com.movie.Repository.ShowSeatsRepository;
import com.movie.Repository.TicketRepository;
import com.movie.Repository.UserRepository;
import com.movie.domain.Show;
import com.movie.domain.ShowSeats;
import com.movie.domain.Ticket;
import com.movie.domain.User;
import com.movie.enums.SeatStatus;
import com.movie.enums.TicketStatus;
import com.movie.exception.ResourceNotFoundException;
import com.movie.resource.BookingEvent;
import com.movie.resource.BookingResource;
import com.movie.resource.TicketMessage;
import com.movie.resource.TicketResource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {
    @Autowired private PaymentService paymentService;

    @Autowired private TicketRepository ticketRepository;

    @Autowired private UserRepository userRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatsRepository showSeatsRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    // ===============================
    // MAIN BOOKING FLOW (NO TX)
    // ===============================9
    public Ticket bookTicketRedis(BookingResource req){

        List<String> lockedKeys = new ArrayList<>();

        // 1. Redis Lock
        lockSeats(req, lockedKeys);

        // 2. Create PENDING ticket
        Ticket ticket = createPendingTicket(req);

        // 3. Publish event (NO PAYMENT HERE)
        try {
            BookingEvent event = new BookingEvent(ticket.getUser().getId(),ticket.getShow().getId(), lockedKeys,0);

            kafkaTemplate.send("BOOKING_CREATED",
                    objectMapper.writeValueAsString(event));

        } catch (Exception e) {
            releaseLocks(lockedKeys);
            throw new RuntimeException("Kafka failure");
        }

        return ticket;
    }
    public TicketResource bookTicket(BookingResource req) {

        List<String> lockedKeys = new ArrayList<>();

        // 1. Redis Lock
        lockSeats(req, lockedKeys);

        // 2. Create PENDING ticket
        Ticket ticket = createPendingTicket(req);

        // 3. Publish event (NO PAYMENT HERE)
        try {
            BookingEvent event = new BookingEvent(ticket.getUser().getId(),ticket.getShow().getId(), lockedKeys,0);

            kafkaTemplate.send("BOOKING_CREATED",
                    objectMapper.writeValueAsString(event));

        } catch (Exception e) {
            releaseLocks(lockedKeys);
            throw new RuntimeException("Kafka failure");
        }

        return Ticket.toResource(ticket);
    }

    // ===============================
    // REDIS LOCKING
    // ===============================
    private void lockSeats(BookingResource req, List<String> lockedKeys) {

        for (String seat : req.getSeatNumbers()) {

            String key = "seat_lock:" + req.getShowId() + ":" + seat;

            Boolean ok = redisTemplate.opsForValue()
                    .setIfAbsent(key,
                            String.valueOf(req.getUserId()),
                            Duration.ofMinutes(5));

            if (Boolean.FALSE.equals(ok)) {
                throw new RuntimeException("Seat already locked: " + seat);
            }

            lockedKeys.add(key);
        }
    }

    // ===============================
    // DB TRANSACTION ONLY
    // ===============================
    @Transactional
    public Ticket createPendingTicket(BookingResource req) {

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Show show = showRepository.findById(req.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        List<ShowSeats> seats =
                showSeatsRepository.findByShowIdAndSeatNumberIn(
                        show.getId(),
                        req.getSeatNumbers()
                );

        for (ShowSeats seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Seat not available");
            }
        }

        // Redis lock
        for (String seatNo : req.getSeatNumbers()) {
            redisTemplate.opsForValue().setIfAbsent(
                    "seat_lock:" + show.getId() + ":" + seatNo,
                    String.valueOf(req.getUserId()),
                    Duration.ofMinutes(5)
            );
        }

        Ticket ticket = Ticket.builder()
                .user(user)
                .show(show)
                .status(TicketStatus.PENDING)
                .allottedSeats(String.join(",", req.getSeatNumbers()))
                .build();

        double total = 0;

        for (ShowSeats seat : seats) {
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedAt(LocalDateTime.now());
            seat.setTicket(ticket);
            total += seat.getRate();
        }

        ticket.setSeats(seats);
        ticket.setAmount(total);

        Ticket saved = ticketRepository.save(ticket);
        showSeatsRepository.saveAll(seats);

        return saved;
    }

    // ===============================
    // SUCCESS FLOW
    // ===============================
    @Transactional
    public void handleSuccess(Long ticketId, List<String> lockedKeys) {

        // RE-FETCH (attached entity)
        Ticket ticket = ticketRepository.findFullTicket(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.CONFIRMED);

        for (ShowSeats seat : ticket.getSeats()) {
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookedAt(LocalDateTime.now());
            seat.setLockedAt(null);
        }

        // NOW SAFE
        String movieName = ticket.getShow().getMovie().getTitle();
        String theaterName = ticket.getShow().getTheater().getName();
        String city = ticket.getShow().getTheater().getCity();

        ticketRepository.save(ticket);

        releaseLocks(lockedKeys);

        sendKafkaEvent(ticket, movieName, theaterName, city);
    }
    // ===============================
    // FAILURE FLOW
    // ===============================
    @Transactional
    public void handleFailure(Long ticketId, List<String> lockedKeys) {

        Ticket ticket = ticketRepository.findFullTicket(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(TicketStatus.FAILED);

        for (ShowSeats seat : ticket.getSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedAt(null);
            seat.setTicket(null);
        }

        ticketRepository.save(ticket);
        showSeatsRepository.saveAll(ticket.getSeats());

        releaseLocks(lockedKeys);
    }

    // ===============================
    // REDIS UNLOCK
    // ===============================
    public void releaseLocks(List<String> keys) {
        for (String key : keys) {
            redisTemplate.delete(key);
        }
    }

    // ===============================
    // KAFKA EVENT
    // ===============================
    private void sendKafkaEvent(Ticket ticket, String movieName, String theaterName, String city) {
        try {
            TicketMessage message = TicketMessage.from(ticket);

            kafkaTemplate.send("TICKET_BOOKED",
                    objectMapper.writeValueAsString(message));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Cacheable(value = "tickets", key = "#id")
    public Ticket getTicketRedis(Long id) {
        System.out.println("Fetching from DB...");
        return ticketRepository.findById(id).orElseThrow();
    }

    public TicketResource getTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with id: " + id));
        return Ticket.toResource(ticket);
    }
//    @Transactional
//    public TicketResource bookTicket(BookingResource bookingResource) {
//
//        User user = userRepository.findById(bookingResource.getUserId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "User not found with id: " + bookingResource.getUserId()));
//
//        Show show = showRepository.findById(bookingResource.getShowId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Show not found with id: " + bookingResource.getShowId()));
//
//        Set<String> requestedSeats = bookingResource.getSeatNumbers();
//        String seatsStr = String.join(",", requestedSeats);
//
//        // Fetch only required seats (OPTIMIZED)
////        List<ShowSeats> seats = showSeatsRepository.findSeatsForUpdate(show.getId(),bookingResource.getSeatNumbers());
//
//        // Validation
////        if (seats.size() != requestedSeats.size()) {
////            throw new ResourceNotFoundException("Some seats not found");
////        }
//
//        // Check availability
////        boolean anyNotAvailable = seats.stream()
////                .anyMatch(s -> s.getStatus() != SeatStatus.AVAILABLE);
////
////        if (anyNotAvailable) {
////            throw new IllegalStateException("Some seats are not available");
////        }
//
////        // Create ticket
////        Ticket ticket = Ticket.builder()
////                .user(user)
////                .show(show)
////                .status(TicketStatus.CONFIRMED)
////                .build();
////        // 🔥 ATOMIC SEAT LOCK DB LEVEL (VERY IMPORTANT)
////        int updatedRows = showSeatsRepository.lockSeats(
////                show.getId(),
////                requestedSeats
////        );
////        if (updatedRows != requestedSeats.size()) {
////            throw new IllegalStateException("Seats already booked or locked by someone else");
////        }
//
//        // Create Ticket (PENDING STATE)
//        Ticket ticket = Ticket.builder()
//                .user(user)
//                .show(show)
//                .status(TicketStatus.PENDING)
//                .allottedSeats(seatsStr)
//                .build();
//
//        // Build seat mapping
//        List<ShowSeats> seats= showSeatsRepository.findByShowIdAndSeatNumberIn(
//                show.getId(),
//                requestedSeats
//        );
//
//        double totalAmount = 0;
//
//        // Process seats
//        for (ShowSeats seat : seats) {
//            seat.setStatus(SeatStatus.BOOKED);
//            seat.setBookedAt(LocalDateTime.now());
//            seat.setTicket(ticket);
//
//            totalAmount += seat.getRate();
//        }
//
//        ticket.setAmount(totalAmount);
//        ticket.setSeats(seats);
//        ticket.setAllottedSeats(seatsStr);
//
//        // Save
//        Ticket savedTicket = ticketRepository.save(ticket);
//        try{
//        TicketMessage message=TicketMessage.builder()
//                .username(savedTicket.getUser().getName())
//                .email(savedTicket.getUser().getEmail())
//                .mobile(savedTicket.getUser().getMobile())
//                .ticketId(savedTicket.getId())
//                .movieName(savedTicket.getShow().getMovie().getTitle())
//                .showTime(savedTicket.getShow().getShowTime().format(DateUtil.FORMATTER))
//                .theaterName(savedTicket.getShow().getTheater().getName())
//                .allottedSeats(savedTicket.getAllottedSeats())
//                .city(savedTicket.getShow().getTheater().getCity())
//                .totalAmount(savedTicket.getAmount())
//                .bookedAt(savedTicket.getBookedAt().format(DateUtil.FORMATTER))
//                .status(savedTicket.getStatus().name())
//                .build();
//
//        kafkaTemplate.send("TICKET_BOOKED",objectMapper.writeValueAsString(message));
//        } catch (Exception e) {
//            throw new RuntimeException("Kafka event failed", e);
//        }
//        return Ticket.toResource(savedTicket);
//    }
//

}
