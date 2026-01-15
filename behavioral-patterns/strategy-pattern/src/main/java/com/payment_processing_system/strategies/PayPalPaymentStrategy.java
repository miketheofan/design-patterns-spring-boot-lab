package com.payment_processing_system.strategies;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;

@Component
public class PayPalPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResponse process(PaymentRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

   @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculateFee'");
    }
}
