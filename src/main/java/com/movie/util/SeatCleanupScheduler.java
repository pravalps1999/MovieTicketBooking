package com.movie.util;

import com.movie.Repository.ShowSeatsRepository;
import com.movie.domain.ShowSeats;
import com.movie.enums.SeatStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SeatCleanupScheduler {

    @Autowired
    private ShowSeatsRepository showSeatsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void releaseExpiredSeats() {

//        List<ShowSeats> lockedSeats = showSeatsRepository.findAllLockedSeats();
        //FETCH ONLY EXPIRED SEATS (OPTIMIZED)
        List<ShowSeats> expiredSeats =
                showSeatsRepository.findByStatusAndLockedAtBefore(
                        SeatStatus.LOCKED,
                        LocalDateTime.now().minusMinutes(1)
                );

        for (ShowSeats seat : expiredSeats) {

            if (seat.getLockedAt() != null &&
                    seat.getLockedAt().isBefore(LocalDateTime.now().minusMinutes(1))) {

                // DB RELEASE
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockedAt(null);
                seat.setTicket(null);

                // REDIS RELEASE
                String key = "seat:" + seat.getShow().getId() + ":" + seat.getSeatNumber();
                redisTemplate.delete(key);
            }
        }

        // VERY IMPORTANT
        showSeatsRepository.saveAll(expiredSeats);
    }
}