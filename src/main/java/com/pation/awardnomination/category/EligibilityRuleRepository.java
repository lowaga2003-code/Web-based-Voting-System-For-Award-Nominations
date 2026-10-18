package com.pation.awardnomination.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule, Long> {

    List<EligibilityRule> findByCategoryIdOrderByIdAsc(Long categoryId);
}
