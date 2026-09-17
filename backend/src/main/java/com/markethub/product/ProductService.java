package com.markethub.product;

import com.markethub.category.*;
import com.markethub.user.*;
import com.markethub.vendor.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository products;
    private final VendorRepository vendors;
    private final CategoryRepository categories;
    private final UserRepository users;

    public ProductService(ProductRepository products, VendorRepository vendors,
                          CategoryRepository categories, UserRepository users) {
        this.products = products; this.vendors = vendors; this.categories = categories; this.users = users;
    }

    @Transactional
    public ProductResponse create(String email, ProductRequest request) {
        Vendor vendor = approvedVendor(email);
        Category category = activeCategory(request.categoryId());
        ProductStatus status = request.stockQuantity() == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE;
        Product product = new Product(vendor, category, request.name().trim(), clean(request.description()),
                request.price(), request.stockQuantity(), clean(request.imageUrl()), status);
        return ProductResponse.from(products.saveAndFlush(product));
    }

    private Vendor approvedVendor(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new VendorAccessDeniedException("Approved vendor profile is required"));
        Vendor vendor = vendors.findByUserId(user.getId())
                .orElseThrow(() -> new VendorAccessDeniedException("Approved vendor profile is required"));
        if (vendor.getStatus() != VendorStatus.APPROVED) {
            throw new VendorAccessDeniedException("Approved vendor profile is required");
        }
        return vendor;
    }

    private Category activeCategory(Long id) {
        Category category = categories.findById(id).orElseThrow(CategoryNotFoundException::new);
        if (!category.isActive()) throw new ProductCategoryInactiveException();
        return category;
    }

    private String clean(String value) { return value == null ? null : value.trim(); }
}
