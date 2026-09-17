package com.markethub.order;
import java.math.BigDecimal;
public record OrderItemResponse(Long id,Long productId,Long vendorId,String productName,BigDecimal unitPrice,int quantity,BigDecimal subtotal,OrderStatus status){static OrderItemResponse from(OrderItem i){return new OrderItemResponse(i.getId(),i.getProductId(),i.getVendorId(),i.getProductName(),i.getUnitPrice(),i.getQuantity(),i.getSubtotal(),i.getStatus());}}
