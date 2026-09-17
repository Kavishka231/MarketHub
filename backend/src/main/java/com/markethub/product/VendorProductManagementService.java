package com.markethub.product;

import com.markethub.category.*;
import com.markethub.user.*;
import com.markethub.vendor.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorProductManagementService {
    private final ProductManagementRepository products;
    private final VendorRepository vendors;
    private final UserRepository users;
    private final CategoryRepository categories;

    public VendorProductManagementService(ProductManagementRepository products, VendorRepository vendors,
            UserRepository users, CategoryRepository categories) {
        this.products = products; this.vendors = vendors; this.users = users; this.categories = categories;
    }

    @Transactional(readOnly = true)
    public ProductPageResponse list(String email, int page, int size) {
        Vendor vendor = vendor(email);
        return ProductPageResponse.from(products.findByVendorId(vendor.getId(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Transactional(readOnly = true)
    public ProductResponse get(String email, Long id) { return ProductResponse.from(owned(email, id)); }

    @Transactional
    public ProductResponse update(String email, Long id, ProductRequest request) {
        Product product = owned(email, id);
        Category category = categories.findById(request.categoryId()).orElseThrow(CategoryNotFoundException::new);
        if (!category.isActive()) throw new ProductCategoryInactiveException();
        product.setName(request.name().trim());
        product.setDescription(clean(request.description()));
        product.setCategory(category);
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(clean(request.imageUrl()));
        if (product.getStatus() != ProductStatus.ARCHIVED) {
            product.setStatus(request.stockQuantity() == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE);
        }
        return ProductResponse.from(products.saveAndFlush(product));
    }

    @Transactional
    public ProductResponse archive(String email, Long id) {
        Product product = owned(email, id);
        product.setStatus(ProductStatus.ARCHIVED);
        return ProductResponse.from(products.saveAndFlush(product));
    }

    private Product owned(String email, Long id) {
        Vendor vendor = vendor(email);
        Product product = products.findById(id).orElseThrow(ProductNotFoundException::new);
        if (!product.getVendor().getId().equals(vendor.getId())) throw new ProductOwnershipException();
        return product;
    }

    private Vendor vendor(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new VendorAccessDeniedException("Approved vendor profile is required"));
        Vendor vendor = vendors.findByUserId(user.getId()).orElseThrow(() -> new VendorAccessDeniedException("Approved vendor profile is required"));
        if (vendor.getStatus() != VendorStatus.APPROVED) throw new VendorAccessDeniedException("Approved vendor profile is required");
        return vendor;
    }

    private String clean(String value) { return value == null ? null : value.trim(); }
}
