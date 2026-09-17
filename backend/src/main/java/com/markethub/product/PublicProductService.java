package com.markethub.product;

import com.markethub.vendor.VendorStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class PublicProductService {
    private final PublicProductRepository products;
    public PublicProductService(PublicProductRepository products) { this.products = products; }

    @Transactional(readOnly = true)
    public ProductPageResponse list(Long categoryId, Long vendorId, BigDecimal minPrice, BigDecimal maxPrice,
            String sort, int page, int size) {
        Specification<Product> spec = visible();
        if (categoryId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        if (vendorId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("vendor").get("id"), vendorId));
        if (minPrice != null) spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        if (maxPrice != null) spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        Sort ordering = switch (sort) {
            case "priceAsc" -> Sort.by("price").ascending();
            case "priceDesc" -> Sort.by("price").descending();
            default -> Sort.by("createdAt").descending();
        };
        return ProductPageResponse.from(products.findAll(spec, PageRequest.of(page, size, ordering)));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = products.findOne(visible().and((root, query, cb) -> cb.equal(root.get("id"), id)))
                .orElseThrow(ProductNotFoundException::new);
        return ProductResponse.from(product);
    }

    private Specification<Product> visible() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), ProductStatus.ACTIVE),
                cb.equal(root.get("vendor").get("status"), VendorStatus.APPROVED),
                cb.isTrue(root.get("category").get("active")));
    }
}
