package com.movie.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.resource.TicketMessage;
import com.movie.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class NotificationConsumer {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    NotificationService notificationService;

    @KafkaListener(topics = "TICKET_BOOKED", groupId = "ticket-group")
    public void sendNotification(String message) throws JsonProcessingException {
        try {
            System.out.println("Received: " + message);

            TicketMessage ticketMessage =
                    objectMapper.readValue(message, TicketMessage.class);

            notificationService.sendNotification(ticketMessage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "ALERTS_TOPIC", groupId = "alert-group")
    public void consumeAlert(String message) throws JsonProcessingException {

        // 1. log
        log.error("SYSTEM ALERT: {}", message);
        // 2. email user
        TicketMessage ticketMessage =
                objectMapper.readValue(message, TicketMessage.class);

        notificationService.sendNotification(ticketMessage);
    }
}
