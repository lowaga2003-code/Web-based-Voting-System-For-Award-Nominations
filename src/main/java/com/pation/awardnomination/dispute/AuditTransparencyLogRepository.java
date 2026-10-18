package com.pation.awardnomination.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for AuditTransparencyLog.
 */
@Repository
public interface AuditTransparencyLogRepository extends JpaRepository<AuditTransparencyLog, Long> {
    List<AuditTransparencyLog> findAllByOrderByTimestampDesc();
    Optional<AuditTransparencyLog> findByTicketId(Long ticketId);
    Optional<AuditTransparencyLog> findByTicketCode(String ticketCode);
    void deleteByTicketId(Long ticketId);
}
