package com.markethub.order;
import java.math.BigDecimal; import java.time.Instant; import java.util.List;
public record OrderDetailResponse(Long id,String orderNumber,OrderStatus status,BigDecimal subtotal,BigDecimal deliveryFee,BigDecimal total,PaymentMethod paymentMethod,String deliveryFullName,String deliveryPhone,String deliveryAddressLine1,String deliveryAddressLine2,String deliveryCity,String deliveryDistrict,String deliveryPostalCode,List<OrderItemResponse> items,Instant createdAt){
 static OrderDetailResponse from(Order o,List<OrderItem> i){return new OrderDetailResponse(o.getId(),o.getOrderNumber(),o.getStatus(),o.getSubtotal(),o.getDeliveryFee(),o.getTotal(),o.getPaymentMethod(),o.getDeliveryFullName(),o.getDeliveryPhone(),o.getDeliveryAddressLine1(),o.getDeliveryAddressLine2(),o.getDeliveryCity(),o.getDeliveryDistrict(),o.getDeliveryPostalCode(),i.stream().map(OrderItemResponse::from).toList(),o.getCreatedAt());}
}
