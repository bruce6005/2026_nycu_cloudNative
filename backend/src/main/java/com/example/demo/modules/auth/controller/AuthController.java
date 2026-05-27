package com.example.demo.modules.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.security.JwtUtil;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody Map<String, String> request) {
        try {
            String credential = request.get("credential");
            if (credential == null || credential.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Credential is required"));
            }

            User user = authService.verifyGoogleToken(credential);
            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(Map.of("user", user, "token", token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }
}
