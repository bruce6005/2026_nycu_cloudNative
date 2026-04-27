package com.example.demo.modules.tempdb.model;

import com.example.demo.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "request")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_user_id", nullable = false)
    private User factoryUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(nullable = false)
    private String priority = "NORMAL";

    @Column(nullable = false)
    private String status = "DRAFT";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "draft_content", columnDefinition = "json")
    private String draftContent;

    @Column(columnDefinition = "TEXT")
    private String description;
}
