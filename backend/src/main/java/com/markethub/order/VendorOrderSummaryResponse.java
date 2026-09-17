package com.markethub.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VendorOrderSummaryResponse(Long orderId, String orderNumber, String customerName,
        OrderStatus status, int vendorItemCount, BigDecimal vendorSubtotal, Instant createdAt) {
    static VendorOrderSummaryResponse from(Order order, List<OrderItem> items) {
        BigDecimal subtotal = items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        String name = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
        return new VendorOrderSummaryResponse(order.getId(), order.getOrderNumber(), name,
                order.getStatus(), items.size(), subtotal, order.getCreatedAt());
    }
}
