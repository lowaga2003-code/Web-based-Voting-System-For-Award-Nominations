package com.pation.awardnomination.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AwardCategoryRepository extends JpaRepository<AwardCategory, Long> {

    List<AwardCategory> findByStatusOrderByVotingStartAsc(CategoryStatus status);

    List<AwardCategory> findByStatusOrderByArchivedAtDesc(CategoryStatus status);

    List<AwardCategory> findByNameIgnoreCase(String name);

    Optional<AwardCategory> findByCode(String code);

    boolean existsByCode(String code);
}
