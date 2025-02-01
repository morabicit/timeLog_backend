package com.example.itida.service;

import com.example.itida.authentication.JwtUtil;
import com.example.itida.dto.UserDto;
import com.example.itida.entity.User;
import com.example.itida.enums.Role;
import com.example.itida.exception.AuthenticationException;
import com.example.itida.exception.EmailAlreadyExists;
import com.example.itida.redisConfig.RedisTokenStore;
import com.example.itida.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenStore redisTokenStore;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RedisTokenStore redisTokenStore) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTokenStore = redisTokenStore;
    }

    public String signup(User user) {
        logger.info("signup() method called");
        logger.debug("Received user details: {}", user);
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new EmailAlreadyExists("Email already exists");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(user.getRole() == null ? Role.USER : user.getRole());
            userRepository.save(user);
            return "User registered successfully!";
        } catch (EmailAlreadyExists e) {
            logger.error("EmailAlreadyExists exception: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during user signup: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while registering the user.");
        }
    }

    public Map<String, String> login(String email, String password) {
        logger.info("Login method called for email: {}", email);
        logger.debug("Login parameters - email: {}, password: [PROTECTED]", email);
        try {
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
                String accessToken = jwtUtil.generateAccessToken(email);
                String refreshToken = jwtUtil.generateRefreshToken(email);
                redisTokenStore.storeAccessToken(accessToken, email);
                redisTokenStore.storeRefreshToken(refreshToken, email);
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);
                tokens.put("userId", String.valueOf(user.get().getId()));
                tokens.put("name", String.valueOf(user.get().getFullName()));
                tokens.put("mobileNumber", String.valueOf(user.get().getMobileNumber()));
                return tokens;
            }
            throw new AuthenticationException("Invalid credentials!");
        }catch (Exception e){
            logger.error("Login failed for email: {}", email, e);
            throw e;
        }
    }

    public String refresh(String refreshToken) {
        logger.info("refresh method called for refresh token");
        try {
            String email = jwtUtil.extractEmail(refreshToken);
            if (email != null && redisTokenStore.isRefreshTokenExists(refreshToken)) {
                String newAccessToken = jwtUtil.generateAccessToken(email);
                redisTokenStore.storeAccessToken(newAccessToken, email);
                return newAccessToken;
            }
            throw new AuthenticationException("Invalid refresh token");
        }catch (Exception e){
            logger.error("failed to generate token for current session", e);
            throw e;
        }
    }

    public void logout(String accessToken, String refreshToken) {
        logger.info("logout() method called");
        logger.debug("Received accessToken: {}, refreshToken: {}", accessToken, refreshToken);
        try {
            String jwtAccessToken = accessToken.substring(7);
            redisTokenStore.deleteAccessToken(jwtAccessToken);
            redisTokenStore.deleteRefreshToken(refreshToken);
            logger.info("Successfully logged out user with accessToken: {}", jwtAccessToken);
        } catch (Exception e) {
            logger.error("Unexpected error during logout: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred during logout.");
        }
    }
}

