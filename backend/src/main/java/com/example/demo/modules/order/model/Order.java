package com.example.demo.modules.order.model;
import jakarta.persistence.*;
@Entity
@Table(name = "cusOrders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Order() {}

    public Order(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}