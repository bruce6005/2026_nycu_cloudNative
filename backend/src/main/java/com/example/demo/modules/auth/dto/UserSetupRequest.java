package com.example.demo.modules.auth.dto;

import com.example.demo.modules.auth.model.UserRole;

import lombok.Data;

@Data
public class UserSetupRequest {

    private UserRole role;

    private Long managerId;
}