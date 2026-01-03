package com.voting.votingapp.Exceptions;

public class PasswordsDoesNotMatch extends RuntimeException {
    public PasswordsDoesNotMatch(String message) {
        super(message);
    }
}
