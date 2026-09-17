package com.markethub.order;
import jakarta.validation.constraints.NotNull;
public record CheckoutRequest(@NotNull(message="Address is required") Long addressId,@NotNull(message="Payment method is required") PaymentMethod paymentMethod) {}
