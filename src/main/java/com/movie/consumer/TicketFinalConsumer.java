package com.movie.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.Repository.TicketRepository;
import com.movie.domain.ShowSeats;
import com.movie.domain.Ticket;
import com.movie.enums.SeatStatus;
import com.movie.enums.TicketStatus;
import com.movie.resource.TicketMessage;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicketFinalConsumer {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "PAYMENT_SUCCESS", groupId = "final-group")
    public void success(String ticketIdStr) throws JsonProcessingException {

        Long ticketId = Long.parseLong(ticketIdStr);

        Ticket ticket = ticketRepository.findFullTicket(ticketId)
                .orElseThrow();

        if (ticket.getStatus() == TicketStatus.CONFIRMED) {
            return; // idempotency
        }

        ticket.setStatus(TicketStatus.CONFIRMED);

        for (ShowSeats seat : ticket.getSeats()) {
            seat.setBookedAt(LocalDateTime.now());
            seat.setStatus(SeatStatus.BOOKED);
        }

        try {
            cacheTicket(ticketRepository.save(ticket));
        } catch (OptimisticLockException e) {
            // retry or send to DLQ
            throw new RuntimeException("Retry required");
        }

        releaseLocks(ticket);
        // THIS IS WHERE TICKET_BOOKED IS PUBLISHED
        try {
            kafkaTemplate.send("TICKET_BOOKED",
                    objectMapper.writeValueAsString(TicketMessage.from(ticket)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @CachePut(value = "tickets", key = "#ticket.id")
    public Ticket cacheTicket(Ticket ticket) {
        return ticket;
    }

    @Transactional
    @KafkaListener(topics = "PAYMENT_FAILED", groupId = "final-group")
    public void failed(String ticketIdStr) throws InterruptedException {

        Long ticketId = Long.parseLong(ticketIdStr);

        Ticket ticket = ticketRepository.findFullTicket(ticketId)
                .orElseThrow();
    Thread.sleep(30000);
        if (ticket.getStatus() == TicketStatus.CONFIRMED) {
            return; // idempotency
        }
        ticket.setStatus(TicketStatus.FAILED);

        for (ShowSeats seat : ticket.getSeats()) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setTicket(null);
            seat.setLockedAt(null);
        }

        ticketRepository.save(ticket);

        releaseLocks(ticket);
    }

    private void releaseLocks(Ticket ticket) {
        String bookingId =ticket.getShow().getId()
                + "_" + ticket.getUser().getId()
                + "_" + String.join(",", ticket.getAllottedSeats());

        redisTemplate.delete("booking:" +bookingId);
    }
}