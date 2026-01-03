package com.voting.votingapp.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateUserDTO {
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
}
