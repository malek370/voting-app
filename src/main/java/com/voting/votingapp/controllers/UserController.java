package com.voting.votingapp.controllers;

import com.voting.votingapp.DTO.AuthentificationResponse;
import com.voting.votingapp.DTO.CreateUserDTO;
import com.voting.votingapp.DTO.LoginDTO;
import com.voting.votingapp.Services.UserService;
import com.voting.votingapp.model.Poll;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDTO user) {
        try {
            AuthentificationResponse authentificationResponse = userService.login(user);
            return ResponseEntity.ok(authentificationResponse);
        } catch (BadCredentialsException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity post(@RequestBody CreateUserDTO  user) {
        try {
            return ResponseEntity.ok(userService.registerUser(user));
        }
        catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.BAD_REQUEST);
        }

    }
     @GetMapping("/test")
    public ResponseEntity test() {
        try {
            //test 2
            return (ResponseEntity) ResponseEntity.ok();
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }
    // @PostMapping("/seed")
    // public void multiple(@RequestBody List<CreateUserDTO> users) {
    // users.forEach(userService::registerUser);
    // }
}
