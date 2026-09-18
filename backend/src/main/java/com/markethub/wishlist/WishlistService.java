package com.markethub.wishlist;
import com.markethub.product.*; import com.markethub.user.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service
public class WishlistService {
 private final WishlistItemRepository items; private final ProductRepository products; private final UserRepository users;
 public WishlistService(WishlistItemRepository items,ProductRepository products,UserRepository users){this.items=items;this.products=products;this.users=users;}
 @Transactional public void add(String email,Long productId){User c=customer(email);Product p=products.findById(productId).orElseThrow(ProductNotFoundException::new);if(items.existsByCustomerIdAndProductId(c.getId(),productId))throw new DuplicateWishlistItemException();items.saveAndFlush(new WishlistItem(c,p));}
 @Transactional public void remove(String email,Long productId){User c=customer(email);WishlistItem item=items.findByCustomerIdAndProductId(c.getId(),productId).orElseThrow(WishlistItemNotFoundException::new);items.delete(item);}
 private User customer(String email){User u=users.findByEmail(email).orElseThrow(()->new WishlistAccessDeniedException("Customer account is required"));if(u.getRole()!=UserRole.CUSTOMER)throw new WishlistAccessDeniedException("Customer role is required");return u;}
}