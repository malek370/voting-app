package com.voting.votingapp.Exceptions;

public class IlligalVote extends RuntimeException {
    public IlligalVote(String message) {
        super(message);
    }
}
