package com.pation.awardnomination.dispute;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Pre-populates H2 in-memory database with initial users, disputes, and logs
 * so the application is immediately testable upon startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DisputeTicketRepository disputeTicketRepository;
    private final SystemActivityLogRepository systemLogRepository;
    private final DisputeNotificationRepository notificationRepository;
    private final AuditLogService auditLogService;

    public DataInitializer(UserRepository userRepository,
                           DisputeTicketRepository disputeTicketRepository,
                           SystemActivityLogRepository systemLogRepository,
                           DisputeNotificationRepository notificationRepository,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.disputeTicketRepository = disputeTicketRepository;
        this.systemLogRepository = systemLogRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // 1. Create Users
        User voter1 = userRepository.save(new User(
                "john_voter",
                "user@govote.net",
                "user123",
                "John Carter",
                UserRole.VOTER
        ));

        User voter2 = userRepository.save(new User(
                "emma_voter",
                "emma@govote.net",
                "user123",
                "Emma Watson",
                UserRole.VOTER
        ));

        User auditor = userRepository.save(new User(
                "vinishkar_auditor",
                "auditor@govote.net",
                "auditor123",
                "Vinishkar R.K. (Compliance Auditor)",
                UserRole.COMPLIANCE_AUDITOR
        ));

        User admin = userRepository.save(new User(
                "sysadmin",
                "admin@govote.net",
                "admin123",
                "System Administrator",
                UserRole.SYSTEM_ADMIN
        ));

        // 2. Pre-seed System Activity Logs
        systemLogRepository.save(new SystemActivityLog(
                "john_voter",
                "VOTE_CAST_SUBMISSION",
                "192.168.1.45",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0",
                "Ballot #8812 cast in category 'Best Director'. Confirmation token dispatched.",
                "NORMAL"
        ));

        systemLogRepository.save(new SystemActivityLog(
                "john_voter",
                "DUPLICATE_BALLOT_ATTEMPT",
                "192.168.1.45",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0",
                "Secondary vote submission attempted within 4 seconds. Rate limiter tripped.",
                "FLAGGED_ANOMALY"
        ));

        systemLogRepository.save(new SystemActivityLog(
                "emma_voter",
                "SESSION_VERIFICATION",
                "10.0.0.12",
                "Safari / iOS 17.2",
                "Ballot verified with dual cryptographic hash confirmation.",
                "NORMAL"
        ));

        // 3. Create Sample Dispute 1: PENDING (New submission awaiting review)
        DisputeTicket ticket1 = new DisputeTicket(
                "DSP-2026-001",
                voter1,
                DisputeCategory.VOTING_ANOMALY,
                "Double Confirmation Prompt during Best Director Vote",
                "When I clicked Submit for my vote in the Best Director category, the screen flickered and showed a submission error, but my ballot ID seemed used.",
                "Screenshot showing error code ERR-BALLOT-409 captured at 20:04 UTC."
        );
        ticket1.setEvidenceFileName("ballot_error_screenshot.png");
        disputeTicketRepository.save(ticket1);

        // 4. Create Sample Dispute 2: UNDER INVESTIGATION (Extension 6a flow)
        DisputeTicket ticket2 = new DisputeTicket(
                "DSP-2026-002",
                voter1,
                DisputeCategory.DUPLICATE_VOTE_DETECTED,
                "False Duplicate Vote Warning: Best Actor Category",
                "Received a warning that I had already cast a vote for Best Actor, but I had only just logged in.",
                "Voter receipt ID #REC-99411 attached."
        );
        ticket2.setEvidenceFileName("voter_receipt.pdf");
        ticket2.escalateToExtendedInvestigation(auditor, "Please provide the exact timestamp on your confirmation email so we can correlate with the server cluster logs.");
        disputeTicketRepository.save(ticket2);

        notificationRepository.save(new DisputeNotification(
                voter1.getId(),
                ticket2.getId(),
                ticket2.getTicketCode(),
                "Investigation Update & Evidence Requested",
                "The Compliance Auditor requested further information: Please provide the exact timestamp on your confirmation email so we can correlate with the server cluster logs.",
                "EVIDENCE_REQUESTED"
        ));

        // 5. Create Sample Dispute 3: RESOLVED & ARCHIVED (Main flow completed)
        DisputeTicket ticket3 = new DisputeTicket(
                "DSP-2026-003",
                voter2,
                DisputeCategory.RESULTS_DISCREPANCY,
                "Category Tally Mismatch for Best Cinematography",
                "Live scoreboard showed total category count 1 vote lower than individual sum.",
                "Photo of public scoreboard stream."
        );
        ticket3.resolveAndArchive(
                auditor,
                "Audit revealed temporary cache latency of 250ms on public dashboard mirror node. Master database recorded all 1,420 votes accurately.",
                "Synchronized edge node cache, verified SHA-256 ballot ledger integrity, and refreshed public analytics stream.",
                "Verified zero vote loss. The tally is 100% consistent with the cryptographically sealed ledger."
        );
        ticket3.acknowledgeResolution();
        DisputeTicket savedResolved = disputeTicketRepository.save(ticket3);

        auditLogService.recordResolutionLog(
                savedResolved,
                auditor.getUsername(),
                savedResolved.getInvestigationFindings(),
                savedResolved.getActionsTaken(),
                savedResolved.getFinalRemarks()
        );
    }
}
