package com.movie.util;

import com.movie.Repository.ShowSeatsRepository;
import com.movie.Repository.TicketRepository;
import com.movie.domain.ShowSeats;
import com.movie.domain.Ticket;
import com.movie.enums.SeatStatus;
import com.movie.enums.TicketStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ExpireTicketScheduler {
    @Autowired private TicketRepository ticketRepository;
    @Autowired private ShowSeatsRepository showSeatsRepository;
    @Autowired private RedisTemplate redisTemplate;
    @Transactional
    @Scheduled(fixedRate = 60000)
    public void expireTickets() {
        List<Ticket> tickets = ticketRepository.findExpiredPendingTickets(LocalDateTime.now().minusMinutes(1));

        for (Ticket ticket : tickets) {
            ticket.setStatus(TicketStatus.EXPIRED);

            for (ShowSeats seat : ticket.getSeats()) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setTicket(null);
                seat.setLockedAt(null);

                // 🔥 Redis cleanup
                String key = "seat_lock:" +
                        ticket.getShow().getId() + ":" +
                        seat.getSeatNumber();

                redisTemplate.delete(key);
            }

            // 🔥 IMPORTANT: save seats explicitly
            showSeatsRepository.saveAll(ticket.getSeats());

            ticketRepository.save(ticket);
        }
    }
}
