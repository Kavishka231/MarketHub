package com.markethub.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorOrderRepository extends JpaRepository<Order, Long> {
    @Query("select distinct o from Order o join OrderItem i on i.order.id = o.id " +
            "where i.vendorId = :vendorId and (:status is null or o.status = :status)")
    Page<Order> findVendorOrders(@Param("vendorId") Long vendorId,
                                 @Param("status") OrderStatus status,
                                 Pageable pageable);
}
