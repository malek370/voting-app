package com.voting.votingapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voting.votingapp.DTO.AuthentificationResponse;
import com.voting.votingapp.DTO.CreateUserDTO;
import com.voting.votingapp.DTO.LoginDTO;
import com.voting.votingapp.Exceptions.EmailExistsException;
import com.voting.votingapp.Exceptions.UsernameExistsException;
import com.voting.votingapp.Services.JwtService;
import com.voting.votingapp.Services.UserDetailsImp;
import com.voting.votingapp.Services.UserService;
import com.voting.votingapp.filter.JwtAuthentificationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthentificationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private LoginDTO loginDTO;
    private CreateUserDTO createUserDTO;
    private AuthentificationResponse authResponse;

    @BeforeEach
    void setUp() {
        loginDTO = new LoginDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password123");

        createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("newuser");
        createUserDTO.setEmail("new@example.com");
        createUserDTO.setPassword("password123");
        createUserDTO.setConfirmPassword("password123");

        authResponse = new AuthentificationResponse("jwt-token", "testuser");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOkWithToken() throws Exception {
        // Arrange
        when(userService.login(any(LoginDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).login(any(LoginDTO.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(userService.login(any(LoginDTO.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/user/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());

        verify(userService).login(any(LoginDTO.class));
    }

    @Test
    void register_WithValidData_ShouldReturnOkWithToken() throws Exception {
        // Arrange
        when(userService.registerUser(any(CreateUserDTO.class))).thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).registerUser(any(CreateUserDTO.class));
    }

    @Test
    void register_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(userService.registerUser(any(CreateUserDTO.class)))
                .thenThrow(new EmailExistsException("Email exists already"));

        // Act & Assert
        mockMvc.perform(post("/api/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());

        verify(userService).registerUser(any(CreateUserDTO.class));
    }

    @Test
    void register_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(userService.registerUser(any(CreateUserDTO.class)))
                .thenThrow(new UsernameExistsException("Username exists already"));

        // Act & Assert
        mockMvc.perform(post("/api/user/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());

        verify(userService).registerUser(any(CreateUserDTO.class));
    }
}
