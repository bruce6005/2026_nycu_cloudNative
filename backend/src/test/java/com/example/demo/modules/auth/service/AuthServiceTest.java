package com.example.demo.modules.auth.service;

import com.example.demo.modules.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    public void testVerifyGoogleToken_InvalidToken_ThrowsException() {
        // 測試 GoogleIdTokenVerifier 對於亂寫的字串是否會拒絕並拋出例外
        Exception exception = assertThrows(Exception.class, () -> {
            authService.verifyGoogleToken("this-is-a-fake-token-that-is-invalid");
        });

        assertNotNull(exception);
    }
}
