package com.markethub.cart;

import com.markethub.auth.JwtService;
import com.markethub.category.*;
import com.markethub.product.*;
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
class CartWorkflowTests {
    @Autowired MockMvc mvc;
    @Autowired CartRepository carts;
    @Autowired CartItemRepository items;
    @Autowired ProductRepository products;
    @Autowired UserRepository users;
    @Autowired VendorRepository vendors;
    @Autowired CategoryRepository categories;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;

    @Test void customerAddsProductAndDuplicateAddUsesOneCartAndOneRow() throws Exception {
        User customer = user("cart-add@example.com", UserRole.CUSTOMER);
        Product product = product("Addable", 10, "12.50", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(customer, product, 2).andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(2));
        add(customer, product, 3).andExpect(status().isCreated()).andExpect(jsonPath("$.quantity").value(5));
        assertThat(carts.count()).isEqualTo(1);
        assertThat(items.count()).isEqualTo(1);
    }

    @Test void unauthenticatedAndVendorRequestsAreRejected() throws Exception {
        Product product = product("Protected", 5, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        mvc.perform(post("/api/cart/items").contentType(MediaType.APPLICATION_JSON).content(addBody(product, 1)))
                .andExpect(status().isUnauthorized());
        User vendor = user("cart-vendor@example.com", UserRole.VENDOR);
        mvc.perform(post("/api/cart/items").header("Authorization", token(vendor))
                .contentType(MediaType.APPLICATION_JSON).content(addBody(product, 1)))
                .andExpect(status().isForbidden());
    }

    @Test void invalidAndAboveStockQuantitiesAreRejected() throws Exception {
        User customer = user("cart-quantity@example.com", UserRole.CUSTOMER);
        Product product = product("Limited", 3, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(customer, product, 0).andExpect(status().isBadRequest());
        add(customer, product, 4).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Requested quantity exceeds available stock"));
    }

    @Test void unavailableProductsAreRejected() throws Exception {
        User customer = user("cart-unavailable@example.com", UserRole.CUSTOMER);
        Product archived = product("Archived", 5, "10", ProductStatus.ARCHIVED, VendorStatus.APPROVED, true);
        Product suspended = product("Suspended", 5, "10", ProductStatus.ACTIVE, VendorStatus.SUSPENDED, true);
        Product inactiveCategory = product("Inactive category", 5, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, false);
        add(customer, archived, 1).andExpect(status().isConflict());
        add(customer, suspended, 1).andExpect(status().isConflict());
        add(customer, inactiveCategory, 1).andExpect(status().isConflict());
    }

    @Test void missingProductReturnsNotFound() throws Exception {
        User customer = user("cart-missing-product@example.com", UserRole.CUSTOMER);
        mvc.perform(post("/api/cart/items").header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content("{\"productId\":999999,\"quantity\":1}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test void viewReturnsCorrectTotalsAndCurrentBackendPrice() throws Exception {
        User customer = user("cart-view@example.com", UserRole.CUSTOMER);
        Product first = product("First", 10, "10.00", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        Product second = product("Second", 10, "5.00", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(customer, first, 2).andExpect(status().isCreated());
        add(customer, second, 3).andExpect(status().isCreated());
        first.setPrice(new BigDecimal("12.00"));
        products.saveAndFlush(first);
        mvc.perform(get("/api/cart").header("Authorization", token(customer)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(12.00))
                .andExpect(jsonPath("$.items[0].lineTotal").value(24.00))
                .andExpect(jsonPath("$.subtotal").value(39.00))
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    @Test void unavailableExistingItemIsIdentifiedWithoutDeletion() throws Exception {
        User customer = user("cart-invalid-item@example.com", UserRole.CUSTOMER);
        Product product = product("Later archived", 5, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(customer, product, 1).andExpect(status().isCreated());
        product.setStatus(ProductStatus.ARCHIVED);
        products.saveAndFlush(product);
        mvc.perform(get("/api/cart").header("Authorization", token(customer)))
                .andExpect(jsonPath("$.items[0].available").value(false));
        assertThat(items.count()).isEqualTo(1);
    }

    @Test void customerUpdatesOwnQuantityAndStockLimitIsEnforced() throws Exception {
        User customer = user("cart-update@example.com", UserRole.CUSTOMER);
        Product product = product("Update", 4, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(customer, product, 1).andExpect(status().isCreated());
        CartItem item = items.findAll().get(0);
        mvc.perform(put("/api/cart/items/{id}", item.getId()).header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(3));
        mvc.perform(put("/api/cart/items/{id}", item.getId()).header("Authorization", token(customer))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":5}"))
                .andExpect(status().isConflict());
    }

    @Test void customerCannotUpdateAnotherCustomersItem() throws Exception {
        User owner = user("cart-owner@example.com", UserRole.CUSTOMER);
        User other = user("cart-other@example.com", UserRole.CUSTOMER);
        Product product = product("Owned", 5, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(owner, product, 1).andExpect(status().isCreated());
        Long itemId = items.findAll().get(0).getId();
        mvc.perform(put("/api/cart/items/{id}", itemId).header("Authorization", token(other))
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test void customerRemovesOwnItemAndAnotherCustomerCannotRemoveIt() throws Exception {
        User owner = user("cart-remove-owner@example.com", UserRole.CUSTOMER);
        User other = user("cart-remove-other@example.com", UserRole.CUSTOMER);
        Product product = product("Remove", 5, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(owner, product, 1).andExpect(status().isCreated());
        Long itemId = items.findAll().get(0).getId();
        mvc.perform(delete("/api/cart/items/{id}", itemId).header("Authorization", token(other)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/cart/items/{id}", itemId).header("Authorization", token(owner)))
                .andExpect(status().isNoContent());
        assertThat(items.findById(itemId)).isEmpty();
    }

    @Test void missingItemReturnsNotFound() throws Exception {
        User customer = user("cart-missing-item@example.com", UserRole.CUSTOMER);
        mvc.perform(delete("/api/cart/items/{id}", Long.MAX_VALUE).header("Authorization", token(customer)))
                .andExpect(status().isNotFound());
    }

    @Test void clearRemovesItemsAndLeavesReusableCart() throws Exception {
        User customer = user("cart-clear@example.com", UserRole.CUSTOMER);
        Product product = product("Clear", 5, "10", ProductStatus.ACTIVE, VendorStatus.APPROVED, true);
        add(customer, product, 2).andExpect(status().isCreated());
        Long cartId = carts.findByCustomerId(customer.getId()).orElseThrow().getId();
        mvc.perform(delete("/api/cart").header("Authorization", token(customer))).andExpect(status().isNoContent());
        assertThat(items.count()).isZero();
        assertThat(carts.findById(cartId)).isPresent();
        mvc.perform(get("/api/cart").header("Authorization", token(customer)))
                .andExpect(jsonPath("$.cartId").value(cartId)).andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.subtotal").value(0)).andExpect(jsonPath("$.totalItems").value(0));
    }

    private org.springframework.test.web.servlet.ResultActions add(User user, Product product, int quantity) throws Exception {
        return mvc.perform(post("/api/cart/items").header("Authorization", token(user))
                .contentType(MediaType.APPLICATION_JSON).content(addBody(product, quantity)));
    }
    private String addBody(Product product, int quantity) {
        return "{\"productId\":" + product.getId() + ",\"quantity\":" + quantity + "}";
    }
    private User user(String email, UserRole role) {
        return users.saveAndFlush(new User("Cart", "Tester", email, encoder.encode("Password123"), role));
    }
    private Product product(String name, int stock, String price, ProductStatus status,
            VendorStatus vendorStatus, boolean categoryActive) {
        String key = name.toLowerCase().replace(' ', '-') + "-" + System.nanoTime();
        User vendorUser = user(key + "@example.com", UserRole.VENDOR);
        Vendor vendor = new Vendor(vendorUser, "Store " + name, "Description", "123");
        vendor.setStatus(vendorStatus); vendor = vendors.saveAndFlush(vendor);
        Category category = new Category("Category " + key, key, "Description");
        category.setActive(categoryActive); category = categories.saveAndFlush(category);
        return products.saveAndFlush(new Product(vendor, category, name, "Description", new BigDecimal(price),
                stock, "https://example.com/item.jpg", status));
    }
    private String token(User user) { return "Bearer " + jwt.generateToken(user); }
}
