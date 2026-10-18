package com.pation.awardnomination.voting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByVoterIdAndCategoryId(Long voterId, Long categoryId);

    List<Vote> findByVoterIdOrderByCastAtDesc(Long voterId);

    List<Vote> findByCategoryId(Long categoryId);

    long countByCategoryId(Long categoryId);

    Optional<Vote> findByReceiptCode(String receiptCode);

    boolean existsByReceiptCode(String receiptCode);

    List<Vote> findByCastAtAfter(LocalDateTime since);

    /** Votes per nominee in one category. */
    default Map<Integer, Long> tallyByNominee(Long categoryId) {
        Map<Integer, Long> tally = new HashMap<>();
        for (Vote v : findByCategoryId(categoryId)) tally.merge(v.getNomineeId(), 1L, Long::sum);
        return tally;
    }
}
