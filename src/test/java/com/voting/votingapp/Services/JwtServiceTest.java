package com.voting.votingapp.Services;

import com.voting.votingapp.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        testUser = new User();
        testUser.setId("test-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void extractClaim_ShouldReturnSubject() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("testuser", subject);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Boolean isValid = jwtService.validateToken(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithDifferentUser_ShouldReturnFalse() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        
        User differentUser = new User();
        differentUser.setUsername("differentuser");

        // Act
        Boolean isValid = jwtService.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractClaim_ShouldReturnIssuedAt() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Long issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt().getTime());

        // Assert
        assertNotNull(issuedAt);
        assertTrue(issuedAt > 0);
        assertTrue(issuedAt <= System.currentTimeMillis());
    }

    @Test
    void extractClaim_ShouldReturnExpiration() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Long expiration = jwtService.extractClaim(token, claims -> claims.getExpiration().getTime());

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration > System.currentTimeMillis());
    }

    @Test
    void generateToken_MultipleTimes_ShouldGenerateDifferentTokens() {
        // Act
        String token1 = jwtService.generateToken(testUser);
        
        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = jwtService.generateToken(testUser);

        // Assert
        assertNotEquals(token1, token2);
    }
}
