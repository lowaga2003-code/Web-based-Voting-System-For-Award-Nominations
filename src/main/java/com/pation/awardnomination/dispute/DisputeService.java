package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Core business service implementing Use Case 6: Manage Dispute.
 * Coordinates Ticket creation, Auditor evaluation, Additional Evidence escalation,
 * Resolution archiving, User Acknowledgement, and Deletion.
 */
@Service
public class DisputeService {

    private final DisputeTicketRepository disputeTicketRepository;
    private final DisputeNotificationRepository notificationRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    @Autowired
    public DisputeService(DisputeTicketRepository disputeTicketRepository,
                          DisputeNotificationRepository notificationRepository,
                          UserService userService,
                          AuditLogService auditLogService) {
        this.disputeTicketRepository = disputeTicketRepository;
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    /**
     * Creates a new dispute ticket (Initiated by Registered User).
     */
    @Transactional
    public DisputeTicket createDispute(Long userId, DisputeCategory category, String title, String description, String evidenceText, String fileName) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        long base = disputeTicketRepository.count() + 1;
        String ticketCode = String.format("DSP-2026-%03d", base);
        while (disputeTicketRepository.findByTicketCode(ticketCode).isPresent()) {
            base++;
            ticketCode = String.format("DSP-2026-%03d", base);
        }

        DisputeTicket ticket = new DisputeTicket(ticketCode, user, category, title, description, evidenceText);
        if (fileName != null && !fileName.isBlank()) {
            ticket.setEvidenceFileName(fileName);
        }

        return disputeTicketRepository.save(ticket);
    }

    public List<DisputeTicket> getDisputesForUser(Long userId) {
        return disputeTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<DisputeTicket> getAllDisputes() {
        return disputeTicketRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<DisputeTicket> getDisputesByStatus(TicketStatus status) {
        return disputeTicketRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public Optional<DisputeTicket> getDisputeById(Long id) {
        return disputeTicketRepository.findById(id);
    }

    /**
     * Branch 1 (Extension Flow 6a): Auditor requires additional investigation / evidence.
     * Updates status to UNDER_INVESTIGATION and notifies the user.
     */
    @Transactional
    public DisputeTicket escalateToUnderInvestigation(Long ticketId, Long auditorId, String evidenceRequestNotes) {
        DisputeTicket ticket = disputeTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + ticketId));

        User auditor = userService.findById(auditorId)
                .orElseThrow(() -> new IllegalArgumentException("Auditor not found: " + auditorId));

        ticket.escalateToExtendedInvestigation(auditor, evidenceRequestNotes);
        DisputeTicket savedTicket = disputeTicketRepository.save(ticket);

        // Step 4 in UML: Notify User (Additional Investigation in process)
        DisputeNotification notification = new DisputeNotification(
                ticket.getUser().getId(),
                ticket.getId(),
                ticket.getTicketCode(),
                "Investigation Update & Evidence Requested",
                "The Compliance Auditor requested further information: " + evidenceRequestNotes,
                "EVIDENCE_REQUESTED"
        );
        notificationRepository.save(notification);

        return savedTicket;
    }

    /**
     * User responds to auditor notification with additional evidence/clarification.
     */
    @Transactional
    public DisputeTicket submitAdditionalEvidence(Long ticketId, Long userId, String additionalEvidence) {
        DisputeTicket ticket = disputeTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + ticketId));

        if (userId != null && !ticket.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Only the dispute creator can submit additional evidence.");
        }

        ticket.provideAdditionalEvidence(additionalEvidence);
        return disputeTicketRepository.save(ticket);
    }

    /**
     * Branch 2 (Main Flow Steps 6-9): Auditor resolves and archives dispute.
     * Updates ticket status to 'Resolved', stores record in Immutable Audit Log,
     * and notifies user of resolution.
     */
    @Transactional
    public DisputeTicket resolveAndArchive(Long ticketId, Long auditorId, String findings, String actionsTaken, String finalRemarks) {
        DisputeTicket ticket = disputeTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + ticketId));

        User auditor = userService.findById(auditorId)
                .orElseThrow(() -> new IllegalArgumentException("Auditor not found: " + auditorId));

        // Step 8: Update ticket status to 'Resolved'
        ticket.resolveAndArchive(auditor, findings, actionsTaken, finalRemarks);
        DisputeTicket savedTicket = disputeTicketRepository.save(ticket);

        // Store log in Immutable Transparency Log
        auditLogService.recordResolutionLog(ticket, auditor.getUsername(), findings, actionsTaken, finalRemarks);

        // Step 10: Notify User of Resolution
        DisputeNotification notification = new DisputeNotification(
                ticket.getUser().getId(),
                ticket.getId(),
                ticket.getTicketCode(),
                "Dispute Resolved & Archived",
                "Your dispute has been resolved by Auditor " + auditor.getFullName() + ". Please review findings.",
                "RESOLVED"
        );
        notificationRepository.save(notification);

        return savedTicket;
    }

    /**
     * Step 10: User acknowledges the final resolution.
     */
    @Transactional
    public DisputeTicket acknowledgeResolution(Long ticketId, Long userId) {
        DisputeTicket ticket = disputeTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + ticketId));

        if (userId != null && !ticket.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Only the ticket creator can acknowledge the dispute resolution.");
        }

        ticket.acknowledgeResolution();
        return disputeTicketRepository.save(ticket);
    }

    public List<DisputeNotification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    public Map<String, Long> getMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("total", disputeTicketRepository.count());
        metrics.put("pending", disputeTicketRepository.countByStatus(TicketStatus.PENDING));
        metrics.put("underInvestigation", disputeTicketRepository.countByStatus(TicketStatus.UNDER_INVESTIGATION));
        metrics.put("resolved", disputeTicketRepository.countByStatus(TicketStatus.RESOLVED));
        return metrics;
    }

    /**
     * Voter deletes/cancels their own dispute ticket.
     */
    @Transactional
    public void deleteDisputeByVoter(Long ticketId, Long userId) {
        DisputeTicket ticket = disputeTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with ID: " + ticketId));

        if (userId != null && !ticket.getUser().getId().equals(userId)) {
            throw new IllegalStateException("You can only cancel or delete your own dispute tickets.");
        }

        notificationRepository.deleteByTicketId(ticketId);
        auditLogService.deleteLogForTicket(ticketId);
        disputeTicketRepository.delete(ticket);
    }

    /**
     * Auditor permanently deletes/purges ONLY resolved/archived tickets.
     */
    @Transactional
    public void deleteDisputeByAuditor(Long ticketId, Long auditorId) {
        DisputeTicket ticket = disputeTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new IllegalStateException("Compliance Auditors can only delete resolved dispute records. Active/Pending disputes cannot be deleted.");
        }

        notificationRepository.deleteByTicketId(ticketId);
        auditLogService.deleteLogForTicket(ticketId);
        disputeTicketRepository.delete(ticket);
    }
}
