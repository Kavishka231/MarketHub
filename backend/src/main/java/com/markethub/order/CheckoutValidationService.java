package com.markethub.order;

import com.markethub.address.*;
import com.markethub.cart.*;
import com.markethub.product.*;
import com.markethub.user.*;
import com.markethub.vendor.VendorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CheckoutValidationService {
 private final UserRepository users; private final CartRepository carts; private final CartItemRepository items; private final AddressRepository addresses; private final BigDecimal deliveryFee;
 public CheckoutValidationService(UserRepository u,CartRepository c,CartItemRepository i,AddressRepository a,@Value("${market-hub.checkout.delivery-fee:300.00}") BigDecimal f){users=u;carts=c;items=i;addresses=a;deliveryFee=f;}
 @Transactional(readOnly=true)
 public CheckoutPlan validate(String email,CheckoutRequest request){
  User customer=users.findByEmail(email).orElseThrow(()->new OrderAccessDeniedException("Customer account is required"));
  if(customer.getRole()!=UserRole.CUSTOMER) throw new OrderAccessDeniedException("Customer role is required");
  Cart cart=carts.findByCustomerId(customer.getId()).orElseThrow(EmptyCartException::new);
  List<CartItem> cartItems=items.findByCartIdOrderByCreatedAtAsc(cart.getId()); if(cartItems.isEmpty()) throw new EmptyCartException();
  Address address=addresses.findById(request.addressId()).orElseThrow(AddressNotFoundException::new);
  if(!address.getUser().getId().equals(customer.getId())) throw new OrderAccessDeniedException("Address belongs to another customer");
  if(request.paymentMethod()!=PaymentMethod.CASH_ON_DELIVERY) throw new CheckoutConflictException("Only cash on delivery is supported");
  BigDecimal subtotal=BigDecimal.ZERO;
  for(CartItem item:cartItems){ Product p=item.getProduct();
   if(p.getStatus()!=ProductStatus.ACTIVE||p.getVendor().getStatus()!=VendorStatus.APPROVED||!p.getCategory().isActive()) throw new CheckoutConflictException("Cart contains an unavailable product: "+p.getId());
   if(item.getQuantity()>p.getStockQuantity()) throw new CheckoutConflictException("Insufficient stock for product: "+p.getId());
   subtotal=subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
  }
  return new CheckoutPlan(customer,cart,cartItems,address,subtotal,deliveryFee,subtotal.add(deliveryFee),request.paymentMethod());
 }
 public CheckoutTotalsResponse totals(String email,CheckoutRequest request){CheckoutPlan p=validate(email,request);return new CheckoutTotalsResponse(p.subtotal(),p.deliveryFee(),p.total());}
}
