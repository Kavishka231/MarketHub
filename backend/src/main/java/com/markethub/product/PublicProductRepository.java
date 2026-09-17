package com.markethub.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PublicProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
}
