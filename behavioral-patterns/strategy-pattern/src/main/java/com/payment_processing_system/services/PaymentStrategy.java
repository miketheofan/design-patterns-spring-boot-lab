package com.payment_processing_system.services;

import org.springframework.stereotype.Service;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;

@Service
public interface PaymentStrategy {
    /**
     * 
     * @param request
     * @return
     */
    public abstract PaymentResponse process(PaymentRequest request);
    public abstract PaymentResponse calculateFee();
}
