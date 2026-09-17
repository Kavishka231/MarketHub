package com.markethub.order;
import java.math.BigDecimal; import java.time.Instant;
public record OrderSummaryResponse(Long id,String orderNumber,OrderStatus status,BigDecimal subtotal,BigDecimal deliveryFee,BigDecimal total,PaymentMethod paymentMethod,Instant createdAt){
 public static OrderSummaryResponse from(Order o){return new OrderSummaryResponse(o.getId(),o.getOrderNumber(),o.getStatus(),o.getSubtotal(),o.getDeliveryFee(),o.getTotal(),o.getPaymentMethod(),o.getCreatedAt());}
}
