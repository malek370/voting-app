package com.voting.votingapp.Exceptions;

public class OptionOrPollNotFound extends RuntimeException {
    public OptionOrPollNotFound(String message) {
        super(message);
    }
}
