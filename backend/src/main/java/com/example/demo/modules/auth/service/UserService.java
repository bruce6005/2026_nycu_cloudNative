package com.example.demo.modules.auth.service;

import org.springframework.stereotype.Service;

import com.example.demo.modules.auth.dto.UserSetupRequest;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.model.UserRole;
import com.example.demo.modules.auth.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User setupProfile(Long userId, UserSetupRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole role = request.getRole();

        if (role == null) {
            throw new RuntimeException("Role is required");
        }

        if (role == UserRole.ADMIN) {
            throw new RuntimeException("Admin role cannot be self-assigned");
        }

        user.setRole(role);

        if (role == UserRole.REQUESTER) {
            Long managerId = request.getManagerId();

            if (managerId == null) {
                throw new RuntimeException("Manager ID is required");
            }

            if (managerId.equals(user.getId())) {
                throw new RuntimeException("Requester cannot select self as manager");
            }

            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager ID does not exist"));

            if (manager.getRole() == null) {
                throw new RuntimeException("Selected manager has no role assigned");
            }

            if (manager.getRole() != UserRole.MANAGER && manager.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Selected user is not a manager");
            }

            user.setManagerId(manager.getId());
        } else {
            user.setManagerId(null);
        }

        return userRepository.save(user);
    }
}