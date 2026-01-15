package com.payment_processing_system.domains;

import java.math.BigDecimal;
import java.util.Map;

import com.payment_processing_system.enums.CurrenciesEnum;
import com.payment_processing_system.enums.PaymentMethodsEnum;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentRequest {

	/**
	 * The payment amount in the specified currency
	 * Must be a positive number greater than zero
	 */
	@NotNull(message = "Amount is required")
	@Positive(message = "Amount must be greater than zero")
	private BigDecimal amount;

	/**
	 * The currency in which the payment is made
	 */
	@NotNull(message = "Currency is required")
	private CurrenciesEnum currency;

	/**
	 * The payment method to be used for processing
	 */
	@NotNull(message = "Payment method is required")
	private PaymentMethodsEnum method;

	/**
	 * Payment-specific details (e.g., card number, PayPal etc)
	 * The structure varies based on the payment method.
	 */
	@NotNull(message = "Payment details are required")
	private Map<String, Object> paymentDetails;
}
