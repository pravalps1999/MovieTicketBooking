package com.movie.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.resource.BookingEvent;
import com.movie.resource.TicketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class DLQConsumer {
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = "BOOKING_FAILED",groupId = "booking-failed-group")
    public void handleFailed(String message) throws Exception {

        BookingEvent event =
                objectMapper.readValue(message, BookingEvent.class);

        if (event.getRetryCount() >= 3) {
            kafkaTemplate.send("BOOKING_DLQ", message);
            return;
        }

        event.setRetryCount(event.getRetryCount() + 1);

        kafkaTemplate.send("BOOKING_RETRY",
                objectMapper.writeValueAsString(event));
    }

    @KafkaListener(topics = "BOOKING_DLQ", groupId = "booking-dlq-group")
    public void handleBookingDLQ(String message) {
        log.error("DLQ received: {}", message);
    }

    @KafkaListener(topics = "TICKET_DLQ", groupId = "ticket-dlq-group")
    public void handleTicketDLQ(String message) {

        try {
            // Convert message for debugging
            TicketMessage event = objectMapper.readValue(message, TicketMessage.class);

            log.error("DLQ triggered for Ticket Saga");
            log.error("TicketId: {}", event.getTicketId());
            log.error("Full Payload: {}", message);

            // Here you can trigger alerts

            sendAlert(event, message);

        } catch (Exception e) {

            log.error("Failed to process DLQ message", e);

            // Even DLQ processing failed → absolute last resort
        }
    }

    private void sendAlert(TicketMessage event, String rawMessage) throws JsonProcessingException {

        // Option 1: log-based alert (basic)
        log.error("ALERT: Ticket saga failed permanently for ticketId={}",
                event.getTicketId());

        // Option 2 (real world): send to notification service                );
        kafkaTemplate.send("ALERTS_TOPIC",
                objectMapper.writeValueAsString(event));

        // Option 3: store in DB (audit table) — recommended in production
    }
}
