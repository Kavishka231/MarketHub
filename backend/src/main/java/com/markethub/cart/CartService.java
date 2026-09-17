package com.markethub.cart;

import com.markethub.product.*;
import com.markethub.user.*;
import com.markethub.vendor.VendorStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductRepository products;
    private final UserRepository users;

    public CartService(CartRepository carts, CartItemRepository items, ProductRepository products, UserRepository users) {
        this.carts = carts; this.items = items; this.products = products; this.users = users;
    }

    @Transactional
    public CartItemResponse add(String email, AddCartItemRequest request) {
        User customer = customer(email);
        Product product = products.findById(request.productId()).orElseThrow(ProductNotFoundException::new);
        requirePurchasable(product);
        Cart cart = carts.findByCustomerId(customer.getId()).orElseGet(() -> carts.save(new Cart(customer)));
        CartItem item = items.findByCartIdAndProductId(cart.getId(), product.getId()).orElse(null);
        int finalQuantity = request.quantity() + (item == null ? 0 : item.getQuantity());
        if (finalQuantity > product.getStockQuantity()) throw new InsufficientStockException();
        if (item == null) item = new CartItem(cart, product, finalQuantity);
        else item.setQuantity(finalQuantity);
        return CartItemResponse.from(items.saveAndFlush(item));
    }

    User customer(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new CartAccessDeniedException("Customer account is required"));
        if (user.getRole() != UserRole.CUSTOMER) throw new CartAccessDeniedException("Customer role is required");
        return user;
    }

    void requirePurchasable(Product product) {
        if (product.getStatus() != ProductStatus.ACTIVE || product.getVendor().getStatus() != VendorStatus.APPROVED
                || !product.getCategory().isActive()) throw new ProductUnavailableException();
    }
}
