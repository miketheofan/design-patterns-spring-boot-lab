package com.payment_processing_system.services;

import org.springframework.stereotype.Service;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;

@Service
public class CreditCardPaymentStrategy implements PaymentStrategy {

    public PaymentResponse process(PaymentRequest request) {

    }

}
