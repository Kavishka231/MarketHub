package com.markethub.order;
import com.markethub.product.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.*;
@Service
public class OrderCancellationService {
 private final OrderQueryService query; private final OrderRepository orders; private final OrderItemRepository items; private final ProductRepository products;
 public OrderCancellationService(OrderQueryService q,OrderRepository o,OrderItemRepository i,ProductRepository p){query=q;orders=o;items=i;products=p;}
 @Transactional public OrderSummaryResponse cancel(String email,Long id){
  Order o=query.owned(email,id); if(o.getStatus()!=OrderStatus.PENDING&&o.getStatus()!=OrderStatus.CONFIRMED)throw new CheckoutConflictException("Order cannot be cancelled in status "+o.getStatus());
  List<OrderItem> snapshots=items.findByOrderIdOrderByIdAsc(id);
  for(OrderItem i:snapshots){i.setStatus(OrderStatus.CANCELLED);products.findById(i.getProductId()).ifPresent(p->{p.setStockQuantity(p.getStockQuantity()+i.getQuantity());if(p.getStatus()==ProductStatus.OUT_OF_STOCK&&p.getStockQuantity()>0)p.setStatus(ProductStatus.ACTIVE);products.save(p);});}
  items.saveAll(snapshots);o.setStatus(OrderStatus.CANCELLED);return OrderSummaryResponse.from(orders.saveAndFlush(o));
 }
}
