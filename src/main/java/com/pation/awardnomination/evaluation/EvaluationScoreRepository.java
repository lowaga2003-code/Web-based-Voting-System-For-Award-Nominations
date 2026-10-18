package com.pation.awardnomination.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** [INTEGRATION - new] table evaluation_scores. */
public interface EvaluationScoreRepository extends JpaRepository<EvaluationScore, Long> {
    List<EvaluationScore> findByEntryId(Long entryId);
    List<EvaluationScore> findByEntryIdAndJudgeId(Long entryId, Long judgeId);
    void deleteByEntryId(Long entryId);
    void deleteByJudgeId(Long judgeId);
    void deleteByCriterionId(Long criterionId);
    void deleteByJudgeIdAndEntryCategoryNameIgnoreCase(Long judgeId, String categoryName);
}
