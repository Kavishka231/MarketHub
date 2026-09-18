package com.markethub.review;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface ReviewRepository extends JpaRepository<Review,Long>{
 boolean existsByCustomerIdAndProductId(Long customerId,Long productId);
 Optional<Review> findByCustomerIdAndProductId(Long customerId,Long productId);
 Page<Review> findByProductId(Long productId,Pageable pageable);
 long countByProductId(Long productId);
 @Query("select coalesce(avg(r.rating),0) from Review r where r.product.id=:productId")
 double averageRatingByProductId(@Param("productId") Long productId);
}