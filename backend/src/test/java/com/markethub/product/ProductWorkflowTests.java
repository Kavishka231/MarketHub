package com.markethub.product;

import com.markethub.auth.JwtService;
import com.markethub.category.*;
import com.markethub.user.*;
import com.markethub.vendor.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductWorkflowTests {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository products;
    @Autowired UserRepository users;
    @Autowired VendorRepository vendors;
    @Autowired CategoryRepository categories;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;

    @Test void productPersistsRelationshipsAndStatus() {
        Vendor vendor = vendor("persist@example.com", VendorStatus.APPROVED);
        Category category = category("Persistence", true);
        Product saved = products.saveAndFlush(product(vendor, category, "Saved", "25.00", 4, ProductStatus.ACTIVE));
        Product found = products.findById(saved.getId()).orElseThrow();
        assertThat(found.getVendor().getId()).isEqualTo(vendor.getId());
        assertThat(found.getCategory().getId()).isEqualTo(category.getId());
        assertThat(found.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test void approvedVendorCreatesProductAndZeroStockSetsOutOfStock() throws Exception {
        Vendor vendor = vendor("create@example.com", VendorStatus.APPROVED);
        Category category = category("Create", true);
        mvc.perform(post("/api/vendor/products").header("Authorization", token(vendor.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("Camera", category, "100.00", 0)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("OUT_OF_STOCK"))
                .andExpect(jsonPath("$.vendorId").value(vendor.getId()));
        assertThat(products.count()).isEqualTo(1);
    }

    @Test void customerAndUnauthenticatedUsersCannotCreate() throws Exception {
        Category category = category("Protected", true);
        User customer = user("customer-product@example.com", UserRole.CUSTOMER);
        mvc.perform(post("/api/vendor/products").header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content(body("No", category, "10", 1)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/vendor/products").contentType(MediaType.APPLICATION_JSON)
                .content(body("No", category, "10", 1))).andExpect(status().isUnauthorized());
    }

    @Test void unapprovedVendorAndInactiveCategoryAreRejected() throws Exception {
        Vendor pending = vendor("pending-product@example.com", VendorStatus.PENDING);
        Category active = category("Active for pending", true);
        mvc.perform(post("/api/vendor/products").header("Authorization", token(pending.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("No", active, "10", 1)))
                .andExpect(status().isForbidden());
        Vendor approved = vendor("inactive-category@example.com", VendorStatus.APPROVED);
        Category inactive = category("Inactive create", false);
        mvc.perform(post("/api/vendor/products").header("Authorization", token(approved.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("No", inactive, "10", 1)))
                .andExpect(status().isBadRequest());
    }

    @Test void invalidPriceAndStockAreRejected() throws Exception {
        Vendor vendor = vendor("invalid-values@example.com", VendorStatus.APPROVED);
        Category category = category("Validation", true);
        mvc.perform(post("/api/vendor/products").header("Authorization", token(vendor.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("Bad", category, "-1", 1)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/vendor/products").header("Authorization", token(vendor.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("Bad", category, "1", -1)))
                .andExpect(status().isBadRequest());
    }

    @Test void vendorListsGetsAndUpdatesOnlyOwnProducts() throws Exception {
        Vendor owner = vendor("owner@example.com", VendorStatus.APPROVED);
        Vendor other = vendor("other@example.com", VendorStatus.APPROVED);
        Category category = category("Manage", true);
        Product own = products.saveAndFlush(product(owner, category, "Old", "20", 2, ProductStatus.ACTIVE));
        Product foreign = products.saveAndFlush(product(other, category, "Foreign", "30", 2, ProductStatus.ACTIVE));
        mvc.perform(get("/api/vendor/products").header("Authorization", token(owner.getUser())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(own.getId()));
        mvc.perform(get("/api/vendor/products/{id}", foreign.getId()).header("Authorization", token(owner.getUser())))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/vendor/products/{id}", own.getId()).header("Authorization", token(owner.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("Updated", category, "50", 0)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.status").value("OUT_OF_STOCK"));
    }

    @Test void vendorCannotUpdateAnotherVendorsProduct() throws Exception {
        Vendor owner = vendor("update-owner@example.com", VendorStatus.APPROVED);
        Vendor other = vendor("update-other@example.com", VendorStatus.APPROVED);
        Category category = category("Ownership", true);
        Product product = products.saveAndFlush(product(other, category, "Private", "20", 2, ProductStatus.ACTIVE));
        mvc.perform(put("/api/vendor/products/{id}", product.getId()).header("Authorization", token(owner.getUser()))
                .contentType(MediaType.APPLICATION_JSON).content(body("Stolen", category, "50", 1)))
                .andExpect(status().isForbidden());
        assertThat(products.findById(product.getId()).orElseThrow().getName()).isEqualTo("Private");
    }

    @Test void archiveKeepsProductButHidesItPublicly() throws Exception {
        Vendor vendor = vendor("archive@example.com", VendorStatus.APPROVED);
        Category category = category("Archive", true);
        Product product = products.saveAndFlush(product(vendor, category, "Archive me", "20", 2, ProductStatus.ACTIVE));
        mvc.perform(patch("/api/vendor/products/{id}/archive", product.getId())
                .header("Authorization", token(vendor.getUser()))).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
        assertThat(products.findById(product.getId())).isPresent();
        mvc.perform(get("/api/products/{id}", product.getId())).andExpect(status().isNotFound());
    }

    @Test void publicListingShowsOnlyEligibleActiveProducts() throws Exception {
        Vendor approved = vendor("public-approved@example.com", VendorStatus.APPROVED);
        Vendor suspended = vendor("public-suspended@example.com", VendorStatus.SUSPENDED);
        Category active = category("Public active", true);
        Category inactive = category("Public inactive", false);
        products.saveAllAndFlush(java.util.List.of(
                product(approved, active, "Visible", "10", 1, ProductStatus.ACTIVE),
                product(approved, active, "Archived", "10", 1, ProductStatus.ARCHIVED),
                product(approved, active, "Inactive", "10", 1, ProductStatus.INACTIVE),
                product(approved, active, "Empty", "10", 0, ProductStatus.OUT_OF_STOCK),
                product(suspended, active, "Suspended", "10", 1, ProductStatus.ACTIVE),
                product(approved, inactive, "Bad category", "10", 1, ProductStatus.ACTIVE)));
        mvc.perform(get("/api/products")).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Visible"));
    }

    @Test void publicFiltersByCategoryVendorAndPrice() throws Exception {
        Vendor first = vendor("filter-first@example.com", VendorStatus.APPROVED);
        Vendor second = vendor("filter-second@example.com", VendorStatus.APPROVED);
        Category books = category("Filter books", true);
        Category toys = category("Filter toys", true);
        products.saveAllAndFlush(java.util.List.of(product(first, books, "Book", "15", 1, ProductStatus.ACTIVE),
                product(first, toys, "Toy", "30", 1, ProductStatus.ACTIVE),
                product(second, books, "Other", "20", 1, ProductStatus.ACTIVE)));
        mvc.perform(get("/api/products").param("categoryId", books.getId().toString())
                .param("vendorId", first.getId().toString()).param("minPrice", "10").param("maxPrice", "18"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Book"));
    }

    @Test void publicSortingSupportsPriceAndNewest() throws Exception {
        Vendor vendor = vendor("sort@example.com", VendorStatus.APPROVED);
        Category category = category("Sort", true);
        products.saveAndFlush(product(vendor, category, "Cheap", "5", 1, ProductStatus.ACTIVE));
        products.saveAndFlush(product(vendor, category, "Expensive", "50", 1, ProductStatus.ACTIVE));
        mvc.perform(get("/api/products").param("sort", "priceAsc"))
                .andExpect(jsonPath("$.content[0].name").value("Cheap"));
        mvc.perform(get("/api/products").param("sort", "priceDesc"))
                .andExpect(jsonPath("$.content[0].name").value("Expensive"));
        mvc.perform(get("/api/products").param("sort", "newest")).andExpect(status().isOk());
    }

    @Test void paginationDefaultsCustomizesCapsAndReportsMetadata() throws Exception {
        Vendor vendor = vendor("pages@example.com", VendorStatus.APPROVED);
        Category category = category("Pages", true);
        for (int i = 0; i < 25; i++) products.save(product(vendor, category, "Item " + i, "10", 1, ProductStatus.ACTIVE));
        products.flush();
        mvc.perform(get("/api/products")).andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20)).andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.content.length()").value(20));
        mvc.perform(get("/api/products").param("page", "1").param("size", "10"))
                .andExpect(jsonPath("$.page").value(1)).andExpect(jsonPath("$.content.length()").value(10));
        mvc.perform(get("/api/products").param("size", "500"))
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test void invalidFiltersAndMissingProductReturnConsistentErrors() throws Exception {
        mvc.perform(get("/api/products").param("minPrice", "20").param("maxPrice", "10"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(get("/api/products").param("sort", "popular"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Product not found"));
    }

    private User user(String email, UserRole role) {
        return users.saveAndFlush(new User("Product", "Tester", email, encoder.encode("Password123"), role));
    }
    private Vendor vendor(String email, VendorStatus status) {
        Vendor vendor = new Vendor(user(email, UserRole.VENDOR), "Store " + email, "Description", "123");
        vendor.setStatus(status); return vendors.saveAndFlush(vendor);
    }
    private Category category(String name, boolean active) {
        Category category = new Category(name, name.toLowerCase().replace(' ', '-'), "Description");
        category.setActive(active); return categories.saveAndFlush(category);
    }
    private Product product(Vendor vendor, Category category, String name, String price, int stock, ProductStatus status) {
        return new Product(vendor, category, name, "Description", new BigDecimal(price), stock, null, status);
    }
    private String token(User user) { return "Bearer " + jwt.generateToken(user); }
    private String body(String name, Category category, String price, int stock) {
        return """
                {"name":"%s","description":"Description","categoryId":%d,"price":%s,"stockQuantity":%d,"imageUrl":"https://example.com/item.jpg"}
                """.formatted(name, category.getId(), price, stock);
    }
}
