package com.pation.awardnomination.voting;

/** Voting - the voter already has a sealed ballot in this category. INHERITANCE: a special VotingException. */
public class DuplicateVoteException extends VotingException {

    private final String existingReceipt;

    public DuplicateVoteException(String message, String existingReceipt) {
        super(message);
        this.existingReceipt = existingReceipt;
    }

    public String getExistingReceipt() {
        return existingReceipt;
    }
}
