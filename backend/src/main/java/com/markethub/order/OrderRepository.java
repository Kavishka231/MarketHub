package com.markethub.order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
public interface OrderRepository extends JpaRepository<Order,Long>{ Page<Order> findByCustomerId(Long id,Pageable p); boolean existsByOrderNumber(String n); }
