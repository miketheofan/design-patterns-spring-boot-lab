package com.payment_processing_system.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@AllArgsConstructor
@RequestMapping("/api/payments")
public class PaymentsController {

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
      @Valid @RequestBody PaymentRequest request) {
        //TODO: process POST request
      
        return null;
    }
    

}
