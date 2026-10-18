package com.pation.awardnomination.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for DisputeNotification.
 */
@Repository
public interface DisputeNotificationRepository extends JpaRepository<DisputeNotification, Long> {
    List<DisputeNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<DisputeNotification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadFalse(Long userId);
    void deleteByTicketId(Long ticketId);
}
