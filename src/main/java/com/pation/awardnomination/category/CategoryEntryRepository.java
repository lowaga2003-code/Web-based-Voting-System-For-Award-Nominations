package com.pation.awardnomination.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryEntryRepository extends JpaRepository<CategoryEntry, Long> {

    List<CategoryEntry> findByCategoryIdOrderByNomineeNameAsc(Long categoryId);

    List<CategoryEntry> findByNomineeId(Integer nomineeId);

    boolean existsByCategoryIdAndNomineeId(Long categoryId, Integer nomineeId);

    long countByCategoryId(Long categoryId);
}
