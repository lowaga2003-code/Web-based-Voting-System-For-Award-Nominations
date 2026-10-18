package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing the Immutable Audit & Transparency Log.
 */
@Service
public class AuditLogService {

    private final AuditTransparencyLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditTransparencyLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public AuditTransparencyLog recordResolutionLog(DisputeTicket ticket, String auditorUsername, String findings, String actionsTaken, String finalRemarks) {
        String actionSummary = "RESOLVE_AND_ARCHIVE: " + actionsTaken;
        AuditTransparencyLog logEntry = new AuditTransparencyLog(
                ticket.getId(),
                ticket.getTicketCode(),
                actionSummary,
                auditorUsername,
                finalRemarks,
                findings
        );
        return auditLogRepository.save(logEntry);
    }

    public List<AuditTransparencyLog> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    public Optional<AuditTransparencyLog> getAuditLogForTicket(Long ticketId) {
        return auditLogRepository.findByTicketId(ticketId);
    }

    @Transactional
    public void deleteLogForTicket(Long ticketId) {
        auditLogRepository.deleteByTicketId(ticketId);
    }
}
