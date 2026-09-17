package com.pation.awardnomination.nominee;

// INHERITANCE: NomineeException is inheriting properties from the built-in Exception class.
public class NomineeException extends Exception {

    // Constructor
    public NomineeException(String errorMessage) {
        super(errorMessage);
    }
}
