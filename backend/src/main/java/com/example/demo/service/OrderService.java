package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
@Service
public class OrderService {

    private final List<String> orders = new ArrayList<>();

    public synchronized void createOrder(String name) {
        System.out.println(LocalDateTime.now() + " [API] POST /orders/generate");
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        orders.add(name.trim());
    }

    public synchronized List<String> getOrders() {
        System.out.println(LocalDateTime.now() + " [API] GET /orders");
        return new ArrayList<>(orders);
    }
}