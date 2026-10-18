package com.pation.awardnomination.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MODULE 6 - table policy_documents.
 */
@Repository
public interface PolicyDocumentRepository extends JpaRepository<PolicyDocument, Long> {

    /** The current (PUBLISHED) version, or the open DRAFT, of one document. */
    Optional<PolicyDocument> findFirstByPolicyTypeAndStatus(PolicyType policyType, PolicyStatus status);

    List<PolicyDocument> findByPolicyType(PolicyType policyType);

    long countByStatus(PolicyStatus status);
}
