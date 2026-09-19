package com.markethub.product;

import com.markethub.review.ReviewRepository;
import com.markethub.vendor.VendorStatus;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicProductService {

    private final PublicProductRepository products;
    private final ReviewRepository reviews;

    public PublicProductService(PublicProductRepository products, ReviewRepository reviews) {
        this.products = products;
        this.reviews = reviews;
    }

    @Transactional(readOnly = true)
    public ProductPageResponse list(
            String search,
            Long categoryId,
            Long vendorId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            int page,
            int size) {
        Specification<Product> specification = visible();
        String term = normalizeSearch(search);

        if (term != null) {
            specification = specification.and((root, query, builder) -> {
                String pattern = "%" + term.toLowerCase(Locale.ROOT) + "%";
                return builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern),
                        builder.like(builder.lower(root.get("category").get("name")), pattern),
                        builder.like(builder.lower(root.get("vendor").get("storeName")), pattern));
            });
        }
        if (categoryId != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("category").get("id"), categoryId));
        }
        if (vendorId != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.equal(root.get("vendor").get("id"), vendorId));
        }
        if (minPrice != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            specification = specification.and(
                    (root, query, builder) -> builder.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Sort ordering = switch (sort) {
            case "priceAsc" -> Sort.by("price").ascending().and(Sort.by("id").ascending());
            case "priceDesc" -> Sort.by("price").descending().and(Sort.by("id").descending());
            default -> Sort.by("createdAt").descending().and(Sort.by("id").descending());
        };

        return ProductPageResponse.from(
                products.findAll(specification, PageRequest.of(page, size, ordering)),
                this::withRating);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = products.findOne(
                visible().and((root, query, builder) -> builder.equal(root.get("id"), id)))
                .orElseThrow(ProductNotFoundException::new);
        return withRating(product);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }

    private ProductResponse withRating(Product product) {
        long count = reviews.countByProductId(product.getId());
        return ProductResponse.from(
                product,
                count == 0 ? 0 : reviews.averageRatingByProductId(product.getId()),
                count);
    }

    private Specification<Product> visible() {
        return (root, query, builder) -> builder.and(
                builder.equal(root.get("status"), ProductStatus.ACTIVE),
                builder.equal(root.get("vendor").get("status"), VendorStatus.APPROVED),
                builder.isTrue(root.get("category").get("active")));
    }
}
