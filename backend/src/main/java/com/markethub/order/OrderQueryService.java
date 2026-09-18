package com.markethub.order;
import com.markethub.user.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class OrderQueryService {
 private final OrderRepository orders; private final OrderItemRepository items; private final UserRepository users;
 public OrderQueryService(OrderRepository o,OrderItemRepository i,UserRepository u){orders=o;items=i;users=u;}
 @Transactional(readOnly=true) public OrderPageResponse list(String email,int page,int size){User c=customer(email);if(page<0||size<1)throw new InvalidOrderRequestException("Invalid pagination");return OrderPageResponse.from(orders.findByCustomerId(c.getId(),PageRequest.of(page,Math.min(size,100),Sort.by("createdAt").descending().and(Sort.by("id").descending()))));}
 @Transactional(readOnly=true) public OrderDetailResponse get(String email,Long id){Order o=owned(email,id);return OrderDetailResponse.from(o,items.findByOrderIdOrderByIdAsc(id));}
 Order owned(String email,Long id){User c=customer(email);Order o=orders.findById(id).orElseThrow(OrderNotFoundException::new);if(!o.getCustomer().getId().equals(c.getId()))throw new OrderOwnershipException();return o;}
 User customer(String email){User u=users.findByEmail(email).orElseThrow(()->new OrderAccessDeniedException("Customer account is required"));if(u.getRole()!=UserRole.CUSTOMER)throw new OrderAccessDeniedException("Customer role is required");return u;}
}
