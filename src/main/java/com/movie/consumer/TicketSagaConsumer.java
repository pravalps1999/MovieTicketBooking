package com.movie.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.domain.Ticket;
import com.movie.resource.BookingResource;
import com.movie.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TicketSagaConsumer {

    @Autowired private TicketService ticketService;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = "BOOKING_VALIDATED", groupId = "ticket-group")
    public void handleBooking(String message) {

        try {
            BookingResource req =
                    objectMapper.readValue(message, BookingResource.class);

            // 1. Redis lock + DB PENDING ticket
            Ticket ticket = ticketService.createPendingTicket(req);

            // 2. Send to payment saga
            kafkaTemplate.send("PAYMENT_REQUEST",
                    ticket.getId().toString());

        } catch (Exception e) {
            kafkaTemplate.send("BOOKING_FAILED", message);
        }
    }
}
