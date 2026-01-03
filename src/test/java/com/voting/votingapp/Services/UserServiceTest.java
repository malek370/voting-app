package com.voting.votingapp.Services;

import com.voting.votingapp.DTO.AuthentificationResponse;
import com.voting.votingapp.DTO.CreateUserDTO;
import com.voting.votingapp.DTO.LoginDTO;
import com.voting.votingapp.Exceptions.EmailExistsException;
import com.voting.votingapp.Exceptions.PasswordsDoesNotMatch;
import com.voting.votingapp.Exceptions.UsernameExistsException;
import com.voting.votingapp.model.User;
import com.voting.votingapp.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private CreateUserDTO createUserDTO;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("test-id");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");

        createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("newuser");
        createUserDTO.setEmail("new@example.com");
        createUserDTO.setPassword("password123");
        createUserDTO.setConfirmPassword("password123");

        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        // Act
        AuthentificationResponse response = userService.login(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> userService.login(loginDTO));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void registerUser_WithValidData_ShouldReturnAuthResponse() throws EmailExistsException, UsernameExistsException {
        // Arrange
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(createUserDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(createUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthentificationResponse response = userService.registerUser(createUserDTO);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(userRepository).existsByUsername(createUserDTO.getUsername());
        verify(passwordEncoder).encode(createUserDTO.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowEmailExistsException() {
        // Arrange
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(true);

        // Act & Assert
        EmailExistsException exception = assertThrows(EmailExistsException.class,
                () -> userService.registerUser(createUserDTO));
        assertEquals("Email exists already", exception.getMessage());
        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldThrowUsernameExistsException() {
        // Arrange
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(createUserDTO.getUsername())).thenReturn(true);

        // Act & Assert
        UsernameExistsException exception = assertThrows(UsernameExistsException.class,
                () -> userService.registerUser(createUserDTO));
        assertEquals("Username exists already", exception.getMessage());
        verify(userRepository).existsByEmail(createUserDTO.getEmail());
        verify(userRepository).existsByUsername(createUserDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithMismatchedPasswords_ShouldThrowPasswordsDoesNotMatch() {
        // Arrange
        createUserDTO.setConfirmPassword("differentPassword");
        when(userRepository.existsByEmail(createUserDTO.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(createUserDTO.getUsername())).thenReturn(false);

        // Act & Assert
        PasswordsDoesNotMatch exception = assertThrows(PasswordsDoesNotMatch.class,
                () -> userService.registerUser(createUserDTO));
        assertEquals("Passwords do not match", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
