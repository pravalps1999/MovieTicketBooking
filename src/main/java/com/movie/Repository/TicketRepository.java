package com.movie.Repository;

import com.movie.domain.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM Ticket t
        JOIN FETCH t.user
        JOIN FETCH t.show s
        JOIN FETCH s.movie
        JOIN FETCH s.theater
        JOIN FETCH t.seats
        WHERE t.id = :id
    """)
    Optional<Ticket> findFullTicket(Long id);

    @Query("SELECT t FROM Ticket t WHERE t.status = 'PENDING' AND t.bookedAt < :time")
    List<Ticket> findExpiredPendingTickets(@Param("time") LocalDateTime time);
}
