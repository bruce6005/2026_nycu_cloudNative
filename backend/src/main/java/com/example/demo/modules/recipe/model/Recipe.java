package com.example.demo.modules.recipe.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "recipe", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"equipment_type", "name"})
})
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_type", nullable = false)
    private String equipmentType;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "json", nullable = false)
    private String parameters;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
