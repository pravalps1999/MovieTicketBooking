package com.movie.service;

import com.movie.domain.Payment;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

    @Autowired
    private RazorpayClient razorpayClient;

    public String createOrder(Payment payment) {

        try {
            JSONObject options = new JSONObject();
            options.put("amount", payment.getAmount() * 100); // paise
            options.put("currency", "INR");
            options.put("receipt", "ticket_" + payment.getTicket().getId());

            Order order = razorpayClient.orders.create(options);

            return order.get("id");

        } catch (Exception e) {
            throw new RuntimeException("Payment order creation failed", e);
        }
    }
}