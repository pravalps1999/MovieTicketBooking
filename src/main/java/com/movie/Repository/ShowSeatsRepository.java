package com.movie.Repository;

import com.movie.domain.ShowSeats;
import com.movie.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ShowSeatsRepository extends JpaRepository<ShowSeats,Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT s FROM ShowSeats s WHERE s.show.id = :showId AND s.seatNumber IN :seatNumbers")
//    List<ShowSeats> findSeatsForUpdate(Long showId, Set<String> seatNumbers);
    @Modifying
    @Query("""
            UPDATE ShowSeats s
            SET s.status = 'LOCKED',
                s.lockedAt = CURRENT_TIMESTAMP
            WHERE s.show.id = :showId
            AND s.seatNumber IN :seatNumbers
            AND s.status = 'AVAILABLE'
        """)
    int lockSeats(Long showId, Set<String> seatNumbers);
    List<ShowSeats> findByShowIdAndSeatNumberIn(Long showId, Set<String> seatNumbers);
    @Query("SELECT s FROM ShowSeats s WHERE s.status = 'LOCKED'")
    List<ShowSeats> findAllLockedSeats();
    List<ShowSeats> findByStatusAndLockedAtBefore(
            SeatStatus status,
            LocalDateTime time
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowSeats s WHERE s.ticket.id = :ticketId")
    List<ShowSeats> findSeatsForUpdate(Long ticketId);
}
