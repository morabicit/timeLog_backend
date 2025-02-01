package com.example.itida.service;

import com.example.itida.dto.UserDto;
import com.example.itida.entity.User;
import com.example.itida.exception.EmailAlreadyExists;
import com.example.itida.exception.UserNotFound;
import com.example.itida.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);
        testUser.setFullName("Test User");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));
        List<UserDto> result = userService.getAllUsers();
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        assertEquals(testEmail, result.get(0).getEmail());
    }

    @Test
    void getUserById_ExistingUse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Optional<UserDto> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
    }

    @Test
    void getUserById_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFound.class,
                () -> userService.getUserById(2L));
    }

    @Test
    void partialUpdateUser_ValidUpdates() {
        Map<String, Object> updates = Map.of(
                "name", "New Name",
                "email", "new@example.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User testUser = inv.getArgument(0);
            testUser.setFullName(updates.get("name").toString());
            testUser.setEmail(updates.get("email").toString());
            return testUser;
        });
        UserDto result = userService.partialUpdateUser(1L, updates);

        assertEquals("New Name", result.getFullName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void partialUpdateUser_emailExist() {
        Map<String, Object> updates = Map.of("email", "test@example.com");
        User existingUser = new User();
        existingUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        assertThrows(EmailAlreadyExists.class,
                () -> userService.partialUpdateUser(1L, updates));
    }

    @Test
    void partialUpdateUser_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFound.class,
                () -> userService.partialUpdateUser(2L, Map.of("name", "Test")));
    }

    @Test
    void deleteUser_ExistingUse() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistent() {
        when(userRepository.existsById(2L)).thenReturn(false);
        assertThrows(UserNotFound.class,
                () -> userService.deleteUser(2L));
    }

    @Test
    void getCurrentUser_AuthenticatedUser_ReturnsUser() {
        setupSecurityContext(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        User result = userService.getCurrentUser();

        assertEquals(testUser, result);
    }

    @Test
    void getCurrentUser_UserNotFound() {
        setupSecurityContext("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class,
                () -> userService.getCurrentUser());
    }

    private void setupSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}
