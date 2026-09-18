package com.markethub.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
    Optional<Review> findByCustomerIdAndProductId(Long customerId, Long productId);
}
