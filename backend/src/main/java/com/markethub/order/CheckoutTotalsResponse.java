package com.markethub.order;
import java.math.BigDecimal;
public record CheckoutTotalsResponse(BigDecimal subtotal,BigDecimal deliveryFee,BigDecimal total) {}
