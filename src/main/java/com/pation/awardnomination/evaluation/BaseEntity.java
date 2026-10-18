package com.pation.awardnomination.evaluation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * OOP - ABSTRACTION + INHERITANCE:
 * Abstract parent holding what every entity shares (id, createdAt).
 * It cannot be instantiated; Judge, Rubric, ShortlistEntry, etc. extend it.
 *
 * [INTEGRATION] moved from com.pation.awardnomination.admin.common (the admin package belongs to Module 1)
 * to this module's own package. No other change.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                       // ENCAPSULATION: private, no setter

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
