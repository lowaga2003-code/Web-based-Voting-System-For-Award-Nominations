package com.pation.awardnomination.evaluation;

/** Thrown when a business rule is broken (duplicate, conflict, weight > 100 ...). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) { super(message); }
}
