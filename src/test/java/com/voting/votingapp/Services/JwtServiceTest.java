package com.voting.votingapp.Services;

import com.voting.votingapp.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
    }

    @Test
    void testGenerateToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }

    @Test
    void testExtractUsername() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void testExtractClaim() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        assertEquals("testuser", subject);
        assertNotNull(issuedAt);
        assertNotNull(expiration);
        assertTrue(expiration.after(issuedAt));
    }

    @Test
    void testValidateToken_Valid() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        Boolean isValid = jwtService.validateToken(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_WrongUser() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        
        User differentUser = new User();
        differentUser.setUsername("differentuser");
        differentUser.setEmail("different@example.com");

        // Act
        Boolean isValid = jwtService.validateToken(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testTokenExpiration() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

        // Act
        long expirationTime = expiration.getTime() - issuedAt.getTime();
        long expectedTime = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        // Assert
        assertEquals(expectedTime, expirationTime);
    }

    @Test
    void testTokenContainsCorrectSubject() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String extractedSubject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals(testUser.getUsername(), extractedSubject);
    }

    @Test
    void testMultipleTokensForSameUser() {
        // Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(testUser);

        // Assert - Both tokens should have same username and be valid
        assertEquals(jwtService.extractUsername(token1), jwtService.extractUsername(token2));
        assertTrue(jwtService.validateToken(token1, testUser));
        assertTrue(jwtService.validateToken(token2, testUser));
    }

    @Test
    void testTokensForDifferentUsers() {
        // Arrange
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        // Act
        String token1 = jwtService.generateToken(testUser);
        String token2 = jwtService.generateToken(user2);

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("testuser", jwtService.extractUsername(token1));
        assertEquals("user2", jwtService.extractUsername(token2));
    }

    @Test
    void testValidateToken_ConsistentResults() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act & Assert - Multiple validations should return the same result
        assertTrue(jwtService.validateToken(token, testUser));
        assertTrue(jwtService.validateToken(token, testUser));
        assertTrue(jwtService.validateToken(token, testUser));
    }

    @Test
    void testExtractUsername_MultipleExtractions() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act - Multiple extractions
        String username1 = jwtService.extractUsername(token);
        String username2 = jwtService.extractUsername(token);
        String username3 = jwtService.extractUsername(token);

        // Assert - Should return same username each time
        assertEquals("testuser", username1);
        assertEquals("testuser", username2);
        assertEquals("testuser", username3);
    }
}
