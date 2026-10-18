package com.pation.awardnomination.evaluation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** [INTEGRATION - new] One judge's marks for one candidate, with the weighted total out of 100. */
public class ScoreSheet {

    private final Judge judge;
    private final Map<Long, Integer> scores = new HashMap<>();   // criterion id -> mark 0-10
    private int revisions;
    private LocalDateTime lastUpdated;
    private Double total;          // null until every criterion has a mark
    private boolean complete;

    public ScoreSheet(Judge judge) {
        this.judge = judge;
    }

    void add(EvaluationScore s) {
        scores.put(s.getCriterion().getId(), s.getScore());
        revisions += s.getRevisions();
        if (lastUpdated == null || (s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(lastUpdated))) {
            lastUpdated = s.getUpdatedAt();
        }
    }

    /** Weighted total: sum of (mark / 10 x criterion weight). With weights adding up to 100 this is out of 100. */
    void calculate(Rubric rubric) {
        if (rubric == null || !rubric.isComplete() || rubric.getCriteria().isEmpty()) {
            complete = false;
            total = null;
            return;
        }
        double sum = 0;
        for (RubricCriterion c : rubric.getCriteria()) {
            Integer mark = scores.get(c.getId());
            if (mark == null) {
                complete = false;
                total = null;
                return;
            }
            sum += mark * (double) c.getWeight() / EvaluationScore.MAX_SCORE;
        }
        complete = true;
        total = Math.round(sum * 10) / 10.0;
    }

    public Judge getJudge() { return judge; }
    public Integer getScore(Long criterionId) { return scores.get(criterionId); }
    public boolean isStarted() { return !scores.isEmpty(); }
    public int getRevisions() { return revisions; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public Double getTotal() { return total; }
    public boolean isComplete() { return complete; }
}
