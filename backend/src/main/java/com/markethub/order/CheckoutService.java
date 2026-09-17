package com.markethub.order;
import com.markethub.cart.*; import com.markethub.product.*; import com.markethub.address.Address;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.time.*; import java.time.format.DateTimeFormatter; import java.util.*;

@Service
public class CheckoutService {
 private final CheckoutValidationService validation; private final OrderRepository orders; private final OrderItemRepository orderItems; private final ProductRepository products; private final CartItemRepository cartItems;
 public CheckoutService(CheckoutValidationService v,OrderRepository o,OrderItemRepository oi,ProductRepository p,CartItemRepository ci){validation=v;orders=o;orderItems=oi;products=p;cartItems=ci;}
 @Transactional
 public OrderSummaryResponse checkout(String email,CheckoutRequest request){
  CheckoutPlan plan=validation.validate(email,request); Address a=plan.address();
  Order order=new Order(plan.customer(),number(),plan.subtotal(),plan.deliveryFee(),plan.paymentMethod(),a.getFullName(),a.getPhone(),a.getAddressLine1(),a.getAddressLine2(),a.getCity(),a.getDistrict(),a.getPostalCode());
  order=orders.saveAndFlush(order); List<OrderItem> snapshots=new ArrayList<>();
  for(CartItem ci:plan.items()){Product p=ci.getProduct(); snapshots.add(new OrderItem(order,p.getId(),p.getVendor().getId(),p.getName(),p.getPrice(),ci.getQuantity())); p.setStockQuantity(p.getStockQuantity()-ci.getQuantity()); if(p.getStockQuantity()==0)p.setStatus(ProductStatus.OUT_OF_STOCK);}
  orderItems.saveAll(snapshots); products.saveAll(plan.items().stream().map(CartItem::getProduct).toList()); cartItems.deleteByCartId(plan.cart().getId());
  return OrderSummaryResponse.from(order);
 }
 private String number(){return "MH-"+LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE)+"-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();}
}
