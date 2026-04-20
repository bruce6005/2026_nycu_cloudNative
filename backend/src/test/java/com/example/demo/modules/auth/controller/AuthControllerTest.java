package com.example.demo.modules.auth.controller;

import com.example.demo.modules.auth.service.AuthService;
import com.example.demo.modules.user.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testAuthenticateGoogle_Success() throws Exception {
        User fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setName("Test User");
        fakeUser.setEmail("test@gmail.com");

        when(authService.verifyGoogleToken(anyString())).thenReturn(fakeUser);

        Map<String, String> request = new HashMap<>();
        request.put("credential", "fake-jwt-token");

        ResponseEntity<?> response = authController.authenticateGoogle(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(body.containsKey("user"));
        User returnedUser = (User) body.get("user");
        assertEquals("Test User", returnedUser.getName());
    }

    @Test
    public void testAuthenticateGoogle_MissingCredential() throws Exception {
        Map<String, String> request = new HashMap<>();

        ResponseEntity<?> response = authController.authenticateGoogle(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Credential is required", body.get("error"));
    }

    @Test
    public void testAuthenticateGoogle_InvalidToken() throws Exception {
        when(authService.verifyGoogleToken(anyString())).thenThrow(new Exception("Invalid ID token."));

        Map<String, String> request = new HashMap<>();
        request.put("credential", "invalid-token");

        ResponseEntity<?> response = authController.authenticateGoogle(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(((String)body.get("error")).contains("Invalid ID token."));
    }
}
