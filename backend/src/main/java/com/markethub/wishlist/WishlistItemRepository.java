package com.markethub.wishlist;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import java.util.Optional;
public interface WishlistItemRepository extends JpaRepository<WishlistItem,Long>{
 boolean existsByCustomerIdAndProductId(Long customerId,Long productId);
 Optional<WishlistItem> findByCustomerIdAndProductId(Long customerId,Long productId);
 @EntityGraph(attributePaths={"product","product.vendor","product.category"})
 Page<WishlistItem> findByCustomerId(Long customerId,Pageable pageable);
}