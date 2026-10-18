package com.pation.awardnomination.category;

/** Module 2 - a business rule was broken; the message is shown to the user as it is. */
public class CategoryException extends RuntimeException {

    public CategoryException(String message) {
        super(message);
    }
}
