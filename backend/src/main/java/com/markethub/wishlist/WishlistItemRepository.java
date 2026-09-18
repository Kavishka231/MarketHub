package com.markethub.wishlist;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface WishlistItemRepository extends JpaRepository<WishlistItem,Long>{
 boolean existsByCustomerIdAndProductId(Long customerId,Long productId);
 Optional<WishlistItem> findByCustomerIdAndProductId(Long customerId,Long productId);
}