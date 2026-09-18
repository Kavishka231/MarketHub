package com.markethub.order;

import com.markethub.user.*;
import com.markethub.vendor.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorOrderService {
    private final VendorOrderRepository orders; private final VendorOrderItemRepository items;
    private final UserRepository users; private final VendorRepository vendors;
    public VendorOrderService(VendorOrderRepository o, VendorOrderItemRepository i, UserRepository u, VendorRepository v) {
        orders=o; items=i; users=u; vendors=v;
    }
    @Transactional(readOnly=true)
    public VendorOrderPageResponse list(String email, OrderStatus status, int page, int size) {
        Vendor vendor = vendor(email);
        if (page < 0 || size < 1) throw new InvalidOrderRequestException("Invalid pagination");
        Page<Order> result = orders.findVendorOrders(vendor.getId(), status,
                PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending().and(Sort.by("id").descending())));
        var content = result.getContent().stream().map(order -> VendorOrderSummaryResponse.from(order,
                items.findByOrderIdAndVendorIdOrderByIdAsc(order.getId(), vendor.getId()))).toList();
        return new VendorOrderPageResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
    Vendor vendor(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new OrderAccessDeniedException("Vendor account is required"));
        if (user.getRole() != UserRole.VENDOR) throw new OrderAccessDeniedException("Vendor role is required");
        Vendor vendor = vendors.findByUserId(user.getId()).orElseThrow(() -> new OrderAccessDeniedException("Vendor profile is required"));
        if (vendor.getStatus() != VendorStatus.APPROVED) throw new OrderAccessDeniedException("Approved vendor profile is required");
        return vendor;
    }
}
