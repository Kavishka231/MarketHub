package com.markethub.order;
import com.markethub.address.Address;
import com.markethub.cart.Cart;
import com.markethub.cart.CartItem;
import com.markethub.user.User;
import java.math.BigDecimal;
import java.util.List;
record CheckoutPlan(User customer,Cart cart,List<CartItem> items,Address address,BigDecimal subtotal,BigDecimal deliveryFee,BigDecimal total,PaymentMethod paymentMethod) {}
