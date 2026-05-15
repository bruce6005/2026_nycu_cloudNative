package com.example.demo.modules.wip_builder.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import com.example.demo.modules.recipe.model.Recipe;
import com.example.demo.modules.equipment.model.Equipment;

@Data
@Entity
@Table(name = "wip_batch")
public class WIPbatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private String status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_end_time")
    private LocalDateTime estimatedEndTime;

    
}
