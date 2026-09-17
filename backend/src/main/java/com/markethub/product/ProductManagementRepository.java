package com.markethub.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductManagementRepository extends JpaRepository<Product, Long> {
    Page<Product> findByVendorId(Long vendorId, Pageable pageable);
}
