package com.example.demo.modules.order.controller;

import org.springframework.web.bind.annotation.*;

import com.example.demo.modules.order.dto.OrderDTO;
import com.example.demo.modules.order.model.Order;
import com.example.demo.modules.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/generate")
    public List<Order> generateOrder(@RequestBody OrderDTO dto) {
        orderService.createOrder(dto.name);
        return orderService.getOrders();
    }

    @GetMapping
    public List<Order> getOrders() {
        return orderService.getOrders();
    }
}