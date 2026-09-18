package com.markethub.review;
import com.markethub.order.*;
import com.markethub.product.*;
import com.markethub.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ReviewService {
 private final ReviewRepository reviews; private final ProductRepository products; private final UserRepository users; private final OrderItemRepository items;
 public ReviewService(ReviewRepository r,ProductRepository p,UserRepository u,OrderItemRepository i){reviews=r;products=p;users=u;items=i;}
 @Transactional public ReviewResponse create(String email,Long productId,ReviewRequest request){
  User c=customer(email); Product p=product(productId);
  if(p.getVendor().getUser().getId().equals(c.getId()))throw new ReviewAccessDeniedException("Customers cannot review their own vendor product");
  if(!items.existsByOrderCustomerIdAndProductIdAndStatus(c.getId(),productId,OrderStatus.DELIVERED))throw new ReviewAccessDeniedException("A delivered purchase is required to review this product");
  if(reviews.existsByCustomerIdAndProductId(c.getId(),productId))throw new DuplicateReviewException();
  return ReviewResponse.from(reviews.saveAndFlush(new Review(c,p,request.rating(),clean(request.comment()))));
 }
 @Transactional public ReviewResponse update(String email,Long productId,ReviewRequest request){User c=customer(email);product(productId);Review r=reviews.findByCustomerIdAndProductId(c.getId(),productId).orElseThrow(ReviewNotFoundException::new);r.setRating(request.rating());r.setComment(clean(request.comment()));return ReviewResponse.from(reviews.saveAndFlush(r));}
 @Transactional public void delete(String email,Long productId){User c=customer(email);product(productId);Review r=reviews.findByCustomerIdAndProductId(c.getId(),productId).orElseThrow(ReviewNotFoundException::new);reviews.delete(r);}
 private User customer(String email){User u=users.findByEmail(email).orElseThrow(()->new ReviewAccessDeniedException("Customer account is required"));if(u.getRole()!=UserRole.CUSTOMER)throw new ReviewAccessDeniedException("Customer role is required");return u;}
 private Product product(Long id){return products.findById(id).orElseThrow(()->new ProductNotFoundException());}
 private String clean(String value){return value==null||value.isBlank()?null:value.trim();}
}