package com.markethub.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderStatusSynchronizationService {
    private final OrderItemRepository items; private final OrderRepository orders;
    public OrderStatusSynchronizationService(OrderItemRepository items, OrderRepository orders) { this.items=items; this.orders=orders; }
    @Transactional
    public void synchronize(Long orderId) {
        Order order=orders.findById(orderId).orElseThrow(OrderNotFoundException::new);
        if(order.getStatus()==OrderStatus.CANCELLED)return;
        List<OrderItem> all=items.findByOrderIdOrderByIdAsc(orderId); if(all.isEmpty())return;
        OrderStatus next;
        if(all.stream().allMatch(i->i.getStatus()==OrderStatus.DELIVERED)) next=OrderStatus.DELIVERED;
        else if(all.stream().allMatch(i->i.getStatus()==OrderStatus.SHIPPED||i.getStatus()==OrderStatus.DELIVERED)) next=OrderStatus.SHIPPED;
        else if(all.stream().anyMatch(i->i.getStatus()==OrderStatus.PROCESSING||i.getStatus()==OrderStatus.SHIPPED||i.getStatus()==OrderStatus.DELIVERED)) next=OrderStatus.PROCESSING;
        else if(all.stream().allMatch(i->i.getStatus()==OrderStatus.CONFIRMED)) next=OrderStatus.CONFIRMED;
        else next=OrderStatus.PENDING;
        order.setStatus(next); orders.save(order);
    }
}
