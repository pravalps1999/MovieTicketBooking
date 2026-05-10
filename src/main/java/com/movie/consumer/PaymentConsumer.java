package com.movie.consumer;

import com.movie.Repository.PaymentRepository;
import com.movie.Repository.TicketRepository;
import com.movie.domain.Payment;
import com.movie.domain.Ticket;
import com.movie.enums.PaymentStatus;
import com.movie.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentConsumer {

    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentService paymentService;

    @KafkaListener(topics = "PAYMENT_REQUEST", groupId = "payment-group")
    public void process(String ticketIdStr) {
        Long ticketId = Long.parseLong(ticketIdStr);

        try {
            log.info(" Payment started for ticketId={}", ticketId);

            // 1. Fetch ticket
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            // 2. Idempotency check (IMPORTANT)
            boolean alreadyProcessed = paymentRepository
                    .existsByTicketIdAndStatus(ticketId, PaymentStatus.SUCCESS);

            if (alreadyProcessed) {
                log.warn("⚠ Payment already processed for ticketId={}", ticketId);
                return;
            }

            // 3. Create payment entry
            Payment payment = new Payment();
            payment.setTicket(ticket);
            payment.setAmount(ticket.getAmount());
            payment.setStatus(PaymentStatus.PENDING);

            payment = paymentRepository.save(payment);

            // 4. Call payment gateway
            boolean success = paymentService.processPayment(payment);

            // 5. Update status
            if (success) {
                payment.setStatus(PaymentStatus.SUCCESS);

                kafkaTemplate.send("PAYMENT_SUCCESS", ticketIdStr);

                log.info("Payment SUCCESS for ticketId={}", ticketId);

            } else {
                payment.setStatus(PaymentStatus.FAILED);

                kafkaTemplate.send("PAYMENT_FAILED", ticketIdStr);

                log.error("Payment FAILED for ticketId={}", ticketId);
            }

            // 6. Save final state
            paymentRepository.save(payment);

        } catch (Exception e) {

            log.error(" Payment processing error for ticketId={}", ticketId, e);

            //  Send to DLQ for retry/analysis
            kafkaTemplate.send("PAYMENT_DLQ", ticketIdStr);
        }
    }
}