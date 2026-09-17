package com.markethub.order;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OrderStatusSynchronizationAspect {
    private final OrderStatusSynchronizationService service;
    public OrderStatusSynchronizationAspect(OrderStatusSynchronizationService service) { this.service=service; }
    @AfterReturning("execution(* com.markethub.order.VendorOrderItemStatusService.update(..)) && args(email,orderId,itemId,request)")
    public void synchronize(String email, Long orderId, Long itemId, VendorOrderItemStatusRequest request) {
        service.synchronize(orderId);
    }
}
