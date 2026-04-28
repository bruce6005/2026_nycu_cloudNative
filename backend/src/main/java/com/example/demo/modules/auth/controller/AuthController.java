package com.example.demo.modules.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody Map<String, String> request) {
        try {
            String credential = request.get("credential");
            if (credential == null || credential.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Credential is required"));
            }
            
            User user = authService.verifyGoogleToken(credential);
            
            // To be secure, here you usually create your own JWT or set an HTTP-only session cookie
            // For this initial setup, we respond with the user data directly.
            // DO NOT use this plain setup for production without issuing your own local session.
            return ResponseEntity.ok(Map.of("user", user, "token", "mock-jwt-token-replace-later"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }
}
