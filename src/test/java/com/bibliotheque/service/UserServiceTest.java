package com.bibliotheque.service;

import com.bibliotheque.model.User;
import com.bibliotheque.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // No specific setup required for all tests, but security context mocks will be set in relevant tests
    }

    @Test
    void testGetCurrentUser_Success() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        
        User user = new User();
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getCurrentUser();

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        Optional<User> result = userService.getCurrentUser();

        assertFalse(result.isPresent());
    }

    @Test
    void testUpdateProfile() {
        User user = new User();
        user.setFirstName("Old");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        user.setFirstName("New");
        User updated = userService.updateProfile(user);

        assertEquals("New", updated.getFirstName());
        verify(userRepository).save(user);
    }

    @Test
    void testChangePassword() {
        User user = new User();
        user.setPassword("oldHash");
        
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(user, "newPass");

        assertEquals("newHash", user.getPassword());
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);
    }

    @Test
    void testCheckPassword() {
        User user = new User();
        user.setPassword("hash");
        
        when(passwordEncoder.matches("raw", "hash")).thenReturn(true);

        assertTrue(userService.checkPassword(user, "raw"));
        verify(passwordEncoder).matches("raw", "hash");
    }
}
