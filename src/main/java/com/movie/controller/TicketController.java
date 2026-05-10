package com.movie.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.domain.Ticket;
import com.movie.resource.BookingResource;
import com.movie.resource.TicketResource;
import com.movie.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ticket")
public class TicketController {
    @Autowired private TicketService ticketService;
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private ObjectMapper objectMapper;

    @PostMapping("/book")
    public String bookTicket(@RequestBody BookingResource req) {

        try {
            // 1. Create booking request event
            String message = objectMapper.writeValueAsString(req);

            // 2. Push to Kafka (START OF SAGA)
            kafkaTemplate.send("BOOKING_REQUEST", message);

            // 3. Return immediately (NON-BLOCKING)
            return "Booking request received. Processing...";

        } catch (Exception e) {
            throw new RuntimeException("Failed to start booking flow");
        }
    }

//    @PostMapping("/book")
//    public ResponseEntity<TicketResource> bookTicket(@Valid @RequestBody BookingResource bookingResource){
//        return ResponseEntity.ok(ticketService.bookTicket(bookingResource));
//    }
//    @PostMapping("/book")
//    public ResponseEntity<String> bookTicket(@RequestBody BookingResource bookingResource) throws JsonProcessingException {
//        String key = bookingResource.getShowId() + "_" + bookingResource.getSeatNumbers().iterator().next();
//        kafkaTemplate.send("BOOKING_REQUEST", key, objectMapper.writeValueAsString(bookingResource));
//        return ResponseEntity.ok("Booking request received");
//    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable(name = "id") @Min(value = 1, message = "Id must be greater than or equal to 1") Long id){
        log.info("Receive request to get ticket with id : "+id);
        return ResponseEntity.ok(ticketService.getTicketRedis(id));
    }
}
