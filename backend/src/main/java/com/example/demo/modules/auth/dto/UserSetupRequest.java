package com.example.demo.modules.auth.dto;

import com.example.demo.modules.auth.model.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSetupRequest {

    private UserRole role;

    private Long managerId;
}