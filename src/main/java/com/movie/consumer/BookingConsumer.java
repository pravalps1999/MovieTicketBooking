package com.movie.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.resource.BookingResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BookingConsumer {

    @Autowired private ObjectMapper objectMapper;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "BOOKING_REQUEST", groupId = "booking-group")
    public void processBooking(String message) {

        try {
            BookingResource req =
                    objectMapper.readValue(message, BookingResource.class);

            // 🔐 Idempotency (correct)
            String bookingId = req.getShowId()
                    + "_" + req.getUserId()
                    + "_" + String.join(",", req.getSeatNumbers());

            String redisKey = "booking:" + bookingId;

            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(1));

            if (Boolean.FALSE.equals(isNew)) {
                return;
            }

            // 👉 NEXT STEP in saga
            kafkaTemplate.send("BOOKING_VALIDATED",
                    objectMapper.writeValueAsString(req));

        } catch (Exception e) {
            kafkaTemplate.send("BOOKING_DLQ", message);
        }
    }
}