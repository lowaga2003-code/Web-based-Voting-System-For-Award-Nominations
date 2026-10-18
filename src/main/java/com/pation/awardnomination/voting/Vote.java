package com.pation.awardnomination.voting;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Voting - one sealed ballot: one voter, one category, one nominee.
 *
 * The unique constraint (voter_id, category_id) is the database-level guarantee of
 * "one vote per category" (Module 1's duplicate-voting protection). Ids are stored as plain
 * columns (no foreign keys), so Module 1, 2 and 3 keep working on their own tables.
 *
 * The seal is a SHA-256 hash of the receipt, category, nominee and a random salt. It is
 * published in the Module 4 transparency log, so changing a stored vote later would be detected.
 */
@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vote_voter_category", columnNames = {"voter_id", "category_id"}),
        @UniqueConstraint(name = "uk_vote_receipt", columnNames = {"receipt_code"})
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Module 1 users.id */
    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    /** Module 2 award_categories.id */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** Module 3 nominees.id */
    @Column(name = "nominee_id", nullable = false)
    private Integer nomineeId;

    @Column(name = "receipt_code", nullable = false, length = 24)
    private String receiptCode;

    @Column(name = "seal_hash", nullable = false, length = 64)
    private String sealHash;

    @Column(name = "seal_salt", nullable = false, length = 32)
    private String sealSalt;

    @Column(name = "cast_at", nullable = false)
    private LocalDateTime castAt;

    public Vote() { }

    public Vote(Long voterId, Long categoryId, Integer nomineeId, String receiptCode, LocalDateTime castAt) {
        this.voterId = voterId;
        this.categoryId = categoryId;
        this.nomineeId = nomineeId;
        this.receiptCode = receiptCode;
        this.castAt = castAt;
    }

    /** ENCAPSULATION: the seal is created and checked by the vote itself. */
    public void seal(String salt) {
        this.sealSalt = salt;
        this.sealHash = BallotSeal.hash(receiptCode, categoryId, nomineeId, salt);
    }

    public boolean isSealIntact() {
        return sealHash != null && sealHash.equals(BallotSeal.hash(receiptCode, categoryId, nomineeId, sealSalt));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVoterId() { return voterId; }
    public Long getCategoryId() { return categoryId; }
    public Integer getNomineeId() { return nomineeId; }
    public String getReceiptCode() { return receiptCode; }
    public String getSealHash() { return sealHash; }
    public String getSealSalt() { return sealSalt; }
    public LocalDateTime getCastAt() { return castAt; }
}
