package com.markethub.order;
import org.springframework.data.jpa.repository.*; import org.springframework.data.domain.*;
public interface OrderRepository extends JpaRepository<Order,Long>,JpaSpecificationExecutor<Order>{Page<Order> findByCustomerId(Long id,Pageable p);boolean existsByOrderNumber(String n);long countByStatus(OrderStatus status);}