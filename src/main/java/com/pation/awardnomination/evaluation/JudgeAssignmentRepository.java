package com.pation.awardnomination.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JudgeAssignmentRepository extends JpaRepository<JudgeAssignment, Long> {
    boolean existsByJudgeIdAndCategoryNameIgnoreCase(Long judgeId, String categoryName);
    void deleteByJudgeId(Long judgeId);
    // [INTEGRATION] judges of one category (scoring page)
    List<JudgeAssignment> findByCategoryNameIgnoreCase(String categoryName);
}
