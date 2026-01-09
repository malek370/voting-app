package com.voting.votingapp.Services;

import com.voting.votingapp.model.User;
import com.voting.votingapp.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsImpTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsImp userDetailsImp;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
    }

    @Test
    void testLoadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsImp.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword123", result.getPassword());
        assertEquals("test@example.com", ((User) result).getEmail());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsImp.loadUserByUsername("nonexistent");
        });
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testLoadUserByUsername_ReturnsUserWithAuthorities() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsImp.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result.getAuthorities());
        assertFalse(result.getAuthorities().isEmpty());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_ReturnsEnabledUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsImp.loadUserByUsername("testuser");

        // Assert
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
    }

    @Test
    void testLoadUserByUsername_DifferentUsers() {
        // Arrange
        User user2 = new User();
        user2.setId("user456");
        user2.setUsername("anotheruser");
        user2.setEmail("another@example.com");
        user2.setPassword("anotherPassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(user2));

        // Act
        UserDetails result1 = userDetailsImp.loadUserByUsername("testuser");
        UserDetails result2 = userDetailsImp.loadUserByUsername("anotheruser");

        // Assert
        assertNotEquals(result1.getUsername(), result2.getUsername());
        assertEquals("testuser", result1.getUsername());
        assertEquals("anotheruser", result2.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByUsername("anotheruser");
    }

    @Test
    void testLoadUserByUsername_CaseSensitive() {
        // Arrange
        when(userRepository.findByUsername("TestUser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert - Different case should not find user
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsImp.loadUserByUsername("TestUser");
        });

        // But correct case should work
        UserDetails result = userDetailsImp.loadUserByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testLoadUserByUsername_ExceptionMessage() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsImp.loadUserByUsername(username);
        });

        assertEquals(username, exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void testLoadUserByUsername_ReturnsActualUserObject() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = userDetailsImp.loadUserByUsername("testuser");

        // Assert
        assertInstanceOf(User.class, result);
        User userResult = (User) result;
        assertEquals("user123", userResult.getId());
        assertEquals("test@example.com", userResult.getEmail());
    }

    @Test
    void testLoadUserByUsername_MultipleCallsSameUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result1 = userDetailsImp.loadUserByUsername("testuser");
        UserDetails result2 = userDetailsImp.loadUserByUsername("testuser");
        UserDetails result3 = userDetailsImp.loadUserByUsername("testuser");

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(result1.getUsername(), result2.getUsername());
        assertEquals(result2.getUsername(), result3.getUsername());
        verify(userRepository, times(3)).findByUsername("testuser");
    }
}
