package com.pation.awardnomination.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShortlistRepository extends JpaRepository<ShortlistEntry, Long> {
    List<ShortlistEntry> findByCategoryNameIgnoreCase(String categoryName);
    List<ShortlistEntry> findByCategoryNameIgnoreCaseAndStatus(String categoryName, ShortlistStatus status);
    boolean existsByNomineeIdAndCategoryNameIgnoreCase(Long nomineeId, String categoryName);
    // [INTEGRATION]
    List<ShortlistEntry> findByStatus(ShortlistStatus status);
    long countByStatus(ShortlistStatus status);
}
