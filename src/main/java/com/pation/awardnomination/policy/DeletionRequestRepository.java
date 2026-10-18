package com.pation.awardnomination.policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MODULE 6 - table data_deletion_requests.
 */
@Repository
public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, Long> {

    List<DeletionRequest> findByUserIdOrderByRequestedAtDesc(Long userId);

    List<DeletionRequest> findAllByOrderByRequestedAtDesc();

    boolean existsByUserIdAndRequestTypeAndStatus(Long userId, DeletionRequestType requestType, RequestStatus status);

    long countByStatus(RequestStatus status);
}
