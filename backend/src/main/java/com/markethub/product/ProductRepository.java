package com.markethub.product;
import org.springframework.data.jpa.repository.*;
public interface ProductRepository extends JpaRepository<Product,Long>,JpaSpecificationExecutor<Product>{long countByStatus(ProductStatus status);}