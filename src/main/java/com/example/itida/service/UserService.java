package com.example.itida.service;

import com.example.itida.dto.UserDto;
import com.example.itida.entity.User;
import com.example.itida.exception.EmailAlreadyExists;
import com.example.itida.exception.UserNotFound;
import com.example.itida.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDto> getAllUsers() {
        logger.info("getAllUsers() called");

        List<User> users = userRepository.findAll();
        if(users.isEmpty()) {
            logger.warn("No users found in database");
        }
        return UserDto.userToUserDto(users);
    }
    public Optional<UserDto> getUserById(Long id) {
        logger.info("getUserById() called for id: {}", id);
        logger.debug("getUserById() called with parameters [id: {}]", id);
        Optional<User> user = userRepository.findById(id);

        if(user.isEmpty()) {
            logger.error("User not found with id: {}", id);
            throw new UserNotFound("User not Found");
        }
        return user.map(UserDto::userToUserDto);
    }

    public UserDto partialUpdateUser(Long id, Map<String, Object> updates) {
        logger.info("partialUpdateUser() called");
        logger.debug("partialUpdateUser() called with parameters [id: {}, updates: {}]", id, updates.keySet());

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found for update with id: {}", id);
                    return new UserNotFound("User not found with id: " + id);
                });

        if (updates.containsKey("email")) {
            String email = (String) updates.get("email");
            logger.debug("Checking email available for: {}", email);

            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                logger.error("Email conflict detected: {}", email);
                throw new EmailAlreadyExists("Email already exists");
            }
        }

        updates.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(User.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, user, value);
            }
        });
        return UserDto.userToUserDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        logger.info("deleteUser() called");
        logger.debug("deleteUser() called with parameters [id: {}]", id);

        if (!userRepository.existsById(id)) {
            logger.error("Delete failed - user not found with id: {}", id);
            throw new UserNotFound("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        logger.info("User {} successfully deleted", id);
    }

    public User getCurrentUser() throws AccessDeniedException {
        logger.info("getCurrentUser() called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.error("No authentication found for curent user");
            throw new AccessDeniedException("No authenticated user found");
        }
        logger.debug("Looking up The user: {}", authentication.getName());
        return  userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> {
                    logger.error("User not found for email: {}", authentication.getName());
                    return new AccessDeniedException("User not found");
                        }
                );
    }
}