package com.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentWebhookController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/webhook")
    public void handleWebhook(@RequestBody String payload) {

        // Razorpay sends payment status here

        boolean success = payload.contains("payment.captured");

        if (success) {
            kafkaTemplate.send("PAYMENT_SUCCESS", payload);
        } else {
            kafkaTemplate.send("PAYMENT_FAILED", payload);
        }
    }
}