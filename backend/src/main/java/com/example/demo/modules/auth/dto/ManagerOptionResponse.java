package com.example.demo.modules.auth.dto;

import com.example.demo.modules.auth.model.UserRole;

public record ManagerOptionResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {
}
