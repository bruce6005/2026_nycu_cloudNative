package com.example.demo.modules.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.modules.auth.dto.ManagerOptionResponse;
import com.example.demo.modules.auth.dto.UserSetupRequest;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/managers")
    public List<ManagerOptionResponse> listManagers() {
        return userService.listManagerOptions();
    }

    @PatchMapping("/{id}/setup")
    public ResponseEntity<?> setupUserProfile(
            @PathVariable Long id,
            @RequestBody UserSetupRequest request
    ) {
        return setupUserProfileById(id, request);
    }

    @PatchMapping("/me/setup")
    public ResponseEntity<?> setupCurrentUserProfile(
            HttpServletRequest httpRequest,
            @RequestBody UserSetupRequest request
    ) {
        User authUser = (User) httpRequest.getAttribute("authUser");
        if (authUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "code", "UNAUTHORIZED",
                            "message", "Missing authenticated user"
                    ));
        }

        return setupUserProfileById(authUser.getId(), request);
    }

    private ResponseEntity<?> setupUserProfileById(Long id, UserSetupRequest request) {
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
