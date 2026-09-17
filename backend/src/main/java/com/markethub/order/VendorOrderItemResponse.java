package com.markethub.order;
import java.math.BigDecimal;
public record VendorOrderItemResponse(Long orderItemId,Long productId,String productName,BigDecimal unitPrice,int quantity,BigDecimal subtotal,OrderStatus status){static VendorOrderItemResponse from(OrderItem i){return new VendorOrderItemResponse(i.getId(),i.getProductId(),i.getProductName(),i.getUnitPrice(),i.getQuantity(),i.getSubtotal(),i.getStatus());}}
