package com.movie.service;


import com.movie.Repository.PaymentRepository;
import com.movie.domain.Payment;
import com.movie.domain.Ticket;
import com.movie.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentGatewayService gatewayService;

    public String initiatePayment(Payment payment) {

        // create order on gateway
        String orderId = gatewayService.createOrder(payment);

        payment.setTransactionId(orderId);

        return orderId;
    }

    public Boolean processPayment(Payment payment) {

        String orderId = gatewayService.createOrder(payment);

        payment.setTransactionId(orderId);

        // WAIT for webhook OR simulate response
        boolean success = Math.random() > 0.2;

        return success;
    }

    public boolean processPayment(Ticket ticket, double amount) {
        Long ticketId= ticket.getId();;
        // IDEMPOTENCY CHECK
        Payment existing = paymentRepository.findByTicketId(ticketId).orElse(null);

        if (existing != null) {
            return existing.getStatus() == PaymentStatus.SUCCESS;
        }

        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setCreatedAt(LocalDateTime.now());

        try {
            payment = paymentRepository.save(payment);
        } catch (Exception e) {
            // another thread already created payment
            Payment alreadyExists = paymentRepository.findByTicketId(ticketId).get();
            return alreadyExists.getStatus() == PaymentStatus.SUCCESS;
        }

        // simulate payment
        boolean success;
        try {
            Thread.sleep(1000);
            success = Math.random() > 0.2;
        } catch (Exception e) {
            success = false;
        }

        payment.setStatus(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setTransactionId("TXN_" + System.currentTimeMillis());

        paymentRepository.save(payment);

        return success;
    }


}