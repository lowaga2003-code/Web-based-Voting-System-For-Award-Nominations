package com.pation.awardnomination.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RubricRepository extends JpaRepository<Rubric, Long> {
    // [INTEGRATION] the rubric used to score a category
    List<Rubric> findByCategoryNameIgnoreCase(String categoryName);
}
