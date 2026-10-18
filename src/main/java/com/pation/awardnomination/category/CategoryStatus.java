package com.pation.awardnomination.category;

/**
 * Module 2 - lifecycle of an award category record.
 * "Delete" in the spec is an ARCHIVE: the row stays for the audit trail.
 */
public enum CategoryStatus {
    ACTIVE,
    ARCHIVED
}
