package com.movie.Repository;

import com.movie.domain.Payment;
import com.movie.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByTicketId(Long ticketId);
    boolean existsByTicketIdAndStatus(Long ticketId, PaymentStatus status);
}
