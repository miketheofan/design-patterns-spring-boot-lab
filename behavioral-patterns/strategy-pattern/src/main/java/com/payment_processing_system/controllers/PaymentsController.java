package com.payment_processing_system.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.services.PaymentService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * REST controller for payment processing operations.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/payments")
public class PaymentsController {

  private final PaymentService paymentService;

  /** Processes a payment using the specified payment method. */
  @PostMapping("/process")
  public ResponseEntity<PaymentResponse> processPayment(
    @Valid @RequestBody PaymentRequest request) {
      PaymentResponse response = paymentService.processPayment(request);
      return ResponseEntity.ok(response);
  }
}
