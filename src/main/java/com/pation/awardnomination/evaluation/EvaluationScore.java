package com.pation.awardnomination.evaluation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * [INTEGRATION - new] One judge's mark (0-10) for one rubric criterion of one shortlisted candidate.
 * SQL Server table: evaluation_scores.
 *
 * Specification "Update: change marks in a re-evaluation" - a mark is changed in place and
 * "revisions" counts how many times it was re-evaluated.
 */
@Entity
@Table(name = "evaluation_scores",
       uniqueConstraints = @UniqueConstraint(name = "uk_evaluation_score", columnNames = {"entry_id", "judge_id", "criterion_id"}))
public class EvaluationScore extends BaseEntity {   // INHERITANCE

    public static final int MAX_SCORE = 10;

    @ManyToOne(optional = false)
    @JoinColumn(name = "entry_id")
    private ShortlistEntry entry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "judge_id")
    private Judge judge;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criterion_id")
    private RubricCriterion criterion;

    private int score;
    private int revisions;
    private LocalDateTime updatedAt;

    protected EvaluationScore() { }   // required by JPA

    public EvaluationScore(ShortlistEntry entry, Judge judge, RubricCriterion criterion, int score) {
        this.entry = entry;
        this.judge = judge;
        this.criterion = criterion;
        this.score = score;
        this.updatedAt = LocalDateTime.now();
    }

    /** ENCAPSULATION: a re-evaluation goes through this method, which keeps the count. */
    public boolean change(int newScore) {
        if (newScore == score) {
            return false;
        }
        this.score = newScore;
        this.revisions++;
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    public ShortlistEntry getEntry() { return entry; }
    public Judge getJudge() { return judge; }
    public RubricCriterion getCriterion() { return criterion; }
    public int getScore() { return score; }
    public int getRevisions() { return revisions; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
