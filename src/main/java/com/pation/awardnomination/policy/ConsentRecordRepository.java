package com.pation.awardnomination.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MODULE 6 - table consent_records.
 */
@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {

    /** A user's consent history, newest first ("Read" part of the CRUD). */
    List<ConsentRecord> findByUserIdOrderByAgreedAtDesc(Long userId);

    List<ConsentRecord> findByUserIdAndStatus(Long userId, ConsentStatus status);

    List<ConsentRecord> findTop50ByOrderByAgreedAtDesc();

    long countByStatus(ConsentStatus status);

    /** How many different users currently agree to one policy version. */
    @Query("select count(distinct c.userId) from ConsentRecord c where c.policyId = :policyId and c.status = :status")
    long countUsersOnPolicy(@Param("policyId") Long policyId, @Param("status") ConsentStatus status);
}
