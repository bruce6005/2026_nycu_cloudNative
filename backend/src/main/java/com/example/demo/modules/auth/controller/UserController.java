package com.example.demo.modules.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.auth.dto.UserSetupRequest;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}/setup")
    public ResponseEntity<?> setupUserProfile(
            @PathVariable Long id,
            @RequestBody UserSetupRequest request
    ) {
        try {
            User updatedUser = userService.setupProfile(id, request);
            return ResponseEntity.ok(Map.of("user", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "code", "INVALID_PROFILE_SETUP",
                            "message", e.getMessage()
                    ));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "code", "RESOURCE_NOT_FOUND",
                            "message", e.getMessage()
                    ));
        }
    }
}