package com.example.demo.modules.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.modules.auth.dto.UserSetupRequest;
import com.example.demo.modules.auth.model.User;
import com.example.demo.modules.auth.model.UserRole ;
import com.example.demo.modules.auth.repository.UserRepository;
import com.example.demo.modules.auth.service.UserService;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserSetupRequest request;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        request = new UserSetupRequest();
    }

    @Test
    void setupProfile_Success_Requester() {
        request.setRole(UserRole.REQUESTER);
        request.setManagerId(2L);

        User manager = new User();
        manager.setId(2L);
        manager.setRole(UserRole.MANAGER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.setupProfile(1L, request);

        assertNotNull(result);
        assertEquals(UserRole.REQUESTER, result.getRole());
        assertEquals(2L, result.getManagerId());
        verify(userRepository).save(testUser);
    }

    @Test
    void setupProfile_Fail_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
    }

    @Test
    void setupProfile_Fail_AdminSelfAssignment() {
        request.setRole(UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
        assertEquals("Admin role cannot be self-assigned", exception.getMessage());
    }

    @Test
    void setupProfile_Fail_RequesterNoManager() {
        request.setRole(UserRole.REQUESTER);
        request.setManagerId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
        assertEquals("Manager ID is required", exception.getMessage());
    }

    @Test
    void setupProfile_Fail_SelfAsManager() {
        request.setRole(UserRole.REQUESTER);
        request.setManagerId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
        assertEquals("Requester cannot select self as manager", exception.getMessage());
    }

    @Test
    void setupProfile_Fail_ManagerNotAManager() {
        request.setRole(UserRole.REQUESTER);
        request.setManagerId(2L);

        User notAManager = new User();
        notAManager.setId(2L);
        notAManager.setRole(UserRole.REQUESTER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(notAManager));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
        assertEquals("Selected user is not a manager", exception.getMessage());
    }

    @Test
    void setupProfile_Fail_RoleIsNull() {
        request.setRole(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
        assertEquals("Role is required", exception.getMessage());
    }

    @Test
    void setupProfile_Fail_ManagerHasNoRole() {
        request.setRole(UserRole.REQUESTER);
        request.setManagerId(2L);

        User managerNoRole = new User();
        managerNoRole.setId(2L);
        managerNoRole.setRole(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerNoRole));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.setupProfile(1L, request));
        assertEquals("Selected manager has no role assigned", exception.getMessage());
    }

    @Test
    void setupProfile_Success_Manager() {
        request.setRole(UserRole.MANAGER);
        request.setManagerId(2L); // Should be ignored/cleared

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.setupProfile(1L, request);

        assertNotNull(result);
        assertEquals(UserRole.MANAGER, result.getRole());
        assertNull(result.getManagerId());
        verify(userRepository).save(testUser);
    }
}
