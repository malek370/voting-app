package com.voting.votingapp.Services;

import com.voting.votingapp.DTO.AuthentificationResponse;
import com.voting.votingapp.DTO.CreateUserDTO;
import com.voting.votingapp.DTO.LoginDTO;
import com.voting.votingapp.Exceptions.EmailExistsException;
import com.voting.votingapp.Exceptions.PasswordsDoesNotMatch;
import com.voting.votingapp.Exceptions.UsernameExistsException;
import com.voting.votingapp.model.User;
import com.voting.votingapp.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    public AuthentificationResponse login(LoginDTO loginDTO) throws BadCredentialsException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken((loginDTO.getUsername()), loginDTO.getPassword())
        );
        User user = userRepository.findByUsername(loginDTO.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);
        return new AuthentificationResponse(token,user.getUsername());
    }
    public AuthentificationResponse registerUser(CreateUserDTO createUserDTO) throws EmailExistsException , UsernameExistsException {
        User user = new User();
        if(userRepository.existsByEmail(createUserDTO.getEmail()))throw new EmailExistsException("Email exists already");
        if(userRepository.existsByUsername(createUserDTO.getUsername()))throw new UsernameExistsException("Username exists already");
        if(!createUserDTO.getPassword().equals(createUserDTO.getConfirmPassword())) throw new PasswordsDoesNotMatch("Passwords do not match");
        user.setUsername(createUserDTO.getUsername());
        user.setEmail(createUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthentificationResponse(token,user.getUsername());    }
}
