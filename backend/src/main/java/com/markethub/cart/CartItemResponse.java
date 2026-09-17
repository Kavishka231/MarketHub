package com.markethub.cart;

import com.markethub.product.Product;
import com.markethub.product.ProductStatus;
import com.markethub.vendor.VendorStatus;
import java.math.BigDecimal;

public record CartItemResponse(Long itemId, Long productId, String productName, String imageUrl,
        BigDecimal unitPrice, int quantity, BigDecimal lineTotal, int stockQuantity,
        boolean available) {
    public static CartItemResponse from(CartItem item) {
        Product product = item.getProduct();
        boolean available = product.getStatus() == ProductStatus.ACTIVE
                && product.getVendor().getStatus() == VendorStatus.APPROVED
                && product.getCategory().isActive();
        return new CartItemResponse(item.getId(), product.getId(), product.getName(), product.getImageUrl(),
                product.getPrice(), item.getQuantity(), product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                product.getStockQuantity(), available);
    }
}
