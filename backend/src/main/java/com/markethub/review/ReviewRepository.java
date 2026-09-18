package com.markethub.review;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
}
