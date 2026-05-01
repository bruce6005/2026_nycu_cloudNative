package com.example.demo.modules.requests.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestsDTO {
    private Long id;
    private String title;
    private String status;

    private Long factoryUserId;
    private Long approverId;
    private Integer priority;
    private String description;
}