package com.example.itida.service;

import com.example.itida.authentication.JwtUtil;
import com.example.itida.entity.User;
import com.example.itida.enums.Role;
import com.example.itida.exception.AuthenticationException;
import com.example.itida.exception.EmailAlreadyExists;
import com.example.itida.redisConfig.RedisTokenStore;
import com.example.itida.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTokenStore redisTokenStore;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
    }

    @Test
    void testSignup_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        String response = authService.signup(user);
        assertEquals("User registered successfully!", response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testSignup_EmailAlreadyExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyExists.class, () -> authService.signup(user));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
        when(jwtUtil.generateAccessToken(user.getEmail())).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(user.getEmail())).thenReturn("refreshToken");

        Map<String, String> tokens = authService.login(user.getEmail(), "password123");

        assertNotNull(tokens);
        assertEquals("accessToken", tokens.get("accessToken"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> authService.login(user.getEmail(), "wrongPassword"));
    }

    @Test
    void testRefresh_Success() {
        String refreshToken = "validRefreshToken";
        when(jwtUtil.extractEmail(refreshToken)).thenReturn(user.getEmail());
        when(redisTokenStore.isRefreshTokenExists(refreshToken)).thenReturn(true);
        when(jwtUtil.generateAccessToken(user.getEmail())).thenReturn("newAccessToken");

        String newToken = authService.refresh(refreshToken);

        assertEquals("newAccessToken", newToken);
    }

    @Test
    void testRefresh_InvalidToken() {
        String refreshToken = "invalidRefreshToken";
        when(jwtUtil.extractEmail(refreshToken)).thenReturn(null);

        assertThrows(AuthenticationException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    void testLogout_Success() {
        String accessToken = "Bearer oldAccessToken";
        String refreshToken = "oldRefreshToken";

        doNothing().when(redisTokenStore).deleteAccessToken("oldAccessToken");
        doNothing().when(redisTokenStore).deleteRefreshToken(refreshToken);

        assertDoesNotThrow(() -> authService.logout(accessToken, refreshToken));
    }
}
