package com.markethub.order;
import java.math.BigDecimal; import java.time.Instant; import java.util.List;
public record VendorOrderDetailResponse(Long orderId,String orderNumber,OrderStatus orderStatus,String customerName,String deliveryCity,String deliveryDistrict,List<VendorOrderItemResponse> vendorItems,BigDecimal vendorSubtotal,Instant createdAt){
 static VendorOrderDetailResponse from(Order o,List<OrderItem> items){BigDecimal subtotal=items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO,BigDecimal::add);String name=o.getCustomer().getFirstName()+" "+o.getCustomer().getLastName();return new VendorOrderDetailResponse(o.getId(),o.getOrderNumber(),o.getStatus(),name,o.getDeliveryCity(),o.getDeliveryDistrict(),items.stream().map(VendorOrderItemResponse::from).toList(),subtotal,o.getCreatedAt());}
}
