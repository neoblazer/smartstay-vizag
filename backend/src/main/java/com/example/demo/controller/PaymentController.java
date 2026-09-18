package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // UPI ID for Paytm QR
    private static final String UPI_ID = "8008152284@ptsbi";

    @GetMapping("/upi-link")
    public ResponseEntity<ApiResponse<String>> generateUpiLink(@RequestParam double amount) {
        String link = "upi://pay?pa=" + UPI_ID +
                "&pn=SmartStay+Vizag" +
                "&am=" + amount +
                "&cu=INR" +
                "&tn=Hotel+Booking+Payment";
        return ResponseEntity.ok(new ApiResponse<>(true, "UPI link generated", link));
    }

    @PostMapping("/pay/{bookingId}")
    public ResponseEntity<ApiResponse<Payment>> processPayment(@PathVariable Long bookingId) {
        Payment payment = paymentService.processPayment(bookingId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment successful", payment));
    }
}