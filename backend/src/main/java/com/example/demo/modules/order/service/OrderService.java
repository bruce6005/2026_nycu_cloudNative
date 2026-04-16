package com.example.demo.modules.order.service;

import com.example.demo.modules.order.model.Order;
import com.example.demo.modules.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repo;

    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }

    public void createOrder(String name) {
        if (name == null || name.trim().isEmpty()) return;

        repo.save(new Order(name.trim()));
    }

    public List<Order> getOrders() {
        return repo.findAll();
    }
}