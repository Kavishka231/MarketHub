package com.markethub.product;

import org.springframework.data.domain.Page;
import java.util.List;

public record ProductPageResponse(List<ProductResponse> content, int page, int size,
        long totalElements, int totalPages) {
    public static ProductPageResponse from(Page<Product> products) {
        return new ProductPageResponse(products.map(ProductResponse::from).getContent(), products.getNumber(),
                products.getSize(), products.getTotalElements(), products.getTotalPages());
    }
}
