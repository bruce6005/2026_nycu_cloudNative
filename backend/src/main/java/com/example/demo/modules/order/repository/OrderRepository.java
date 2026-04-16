package com.example.demo.modules.order.repository;

import com.example.demo.modules.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}