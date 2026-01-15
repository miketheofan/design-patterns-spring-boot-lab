package com.payment_processing_system.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.payment_processing_system.domains.PaymentRequest;
import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.PaymentMethodsEnum;
import com.payment_processing_system.exceptions.UnsupportedPaymentMethodException;
import com.payment_processing_system.strategies.PaymentStrategy;

import lombok.AllArgsConstructor;

/**
 * Central payment processing service implementing the Strategy pattern.
 * Delegates payment processing to the appropriate strategy based on the payment method.
 */
@Service
@AllArgsConstructor
public class PaymentService {

    private final Map<PaymentMethodsEnum, PaymentStrategy> paymentStrategies;

    /**
     * Processes a payment by selecting and executing the appropriate payment strategy.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        PaymentStrategy strategy = paymentStrategies.get(request.getMethod());
        if (strategy == null) {
            throw new UnsupportedPaymentMethodException(
                "Payment method " + request.getMethod().getName() +
                " is not currently supported"
            );
        }

        return strategy.process(request);
    }
}
