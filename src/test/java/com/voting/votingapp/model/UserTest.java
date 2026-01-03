package com.voting.votingapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("test-id");
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
    }

    @Test
    void getAuthorities_ShouldReturnUserRole() {
        // Act
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(user.isEnabled());
    }

    @Test
    void userDetails_ShouldContainCorrectInformation() {
        // Assert
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("test-id", user.getId());
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        User newUser = new User();

        // Act
        newUser.setId("new-id");
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("newpass");

        // Assert
        assertEquals("new-id", newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("new@example.com", newUser.getEmail());
        assertEquals("newpass", newUser.getPassword());
    }
}
