package com.pation.awardnomination.evaluation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** INHERITANCE: a more specific kind of BusinessRuleException. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends BusinessRuleException {
    public ResourceNotFoundException(String message) { super(message); }
}
