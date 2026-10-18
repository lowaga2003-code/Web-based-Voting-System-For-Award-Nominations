package com.pation.awardnomination.voting;

/** Voting - a rule stopped the vote; the message is shown to the voter. */
public class VotingException extends RuntimeException {

    public VotingException(String message) {
        super(message);
    }
}
