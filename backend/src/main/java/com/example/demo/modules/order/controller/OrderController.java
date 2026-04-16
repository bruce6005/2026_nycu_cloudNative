package com.example.demo.modules.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.modules.order.dto.OrderDTO;
import com.example.demo.modules.order.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/generate")
    public List<String> generateOrder(@RequestBody OrderDTO dto) {
        orderService.createOrder(dto.name);
        return orderService.getOrders();
    }

    @GetMapping
    public List<String> getOrders() {
        return orderService.getOrders();
    }
}