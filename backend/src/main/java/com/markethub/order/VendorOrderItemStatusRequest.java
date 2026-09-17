package com.markethub.order;
import jakarta.validation.constraints.NotNull;
public record VendorOrderItemStatusRequest(@NotNull(message="Status is required") OrderStatus status) {}
