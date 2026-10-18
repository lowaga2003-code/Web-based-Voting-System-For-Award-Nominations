package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Spring MVC Controller serving JSP views for Dispute & Audit Management (UC-6).
 * Follows the same design and architectural patterns as NomineeController.
 */
@Controller
public class DisputeViewController {

    private final DisputeService disputeService;
    private final AuditLogService auditLogService;
    private final SystemLogService systemLogService;
    private final UserService userService;

    @Autowired
    public DisputeViewController(DisputeService disputeService,
                                 AuditLogService auditLogService,
                                 SystemLogService systemLogService,
                                 UserService userService) {
        this.disputeService = disputeService;
        this.auditLogService = auditLogService;
        this.systemLogService = systemLogService;
        this.userService = userService;
    }

    // [INTEGRATION] REMOVED the "/" mapping (default entry point -> /dispute/list).
    // In the merged system "/" opens the main dashboard (DashboardController); two handlers for "/"
    // stop Spring Boot from starting. The dashboard's Module 4 card links to these pages instead.

    // ==========================================
    // 1. REGISTERED USER / VOTER VIEWS
    // ==========================================

    @GetMapping("/dispute/list")
    public String viewDisputeList(@RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
                                  Model model) {
        List<DisputeTicket> tickets = disputeService.getDisputesForUser(userId);
        List<DisputeNotification> notifications = disputeService.getNotificationsForUser(userId);

        User currentUser = userService.findById(userId).orElse(null);

        model.addAttribute("tickets", tickets);
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("userId", userId);
        return "disputeList";
    }

    @GetMapping("/dispute/form")
    public String showDisputeForm(@RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
                                  Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("categories", DisputeCategory.values());
        return "disputeForm";
    }

    @PostMapping("/dispute/submit")
    public String submitDispute(@RequestParam("userId") Long userId,
                                @RequestParam("category") DisputeCategory category,
                                @RequestParam("title") String title,
                                @RequestParam("description") String description,
                                @RequestParam(value = "evidenceText", required = false) String evidenceText,
                                @RequestParam(value = "evidenceFile", required = false) MultipartFile evidenceFile,
                                RedirectAttributes redirectAttributes) {
        try {
            String fileName = null;
            if (evidenceFile != null && !evidenceFile.isEmpty()) {
                String originalFilename = evidenceFile.getOriginalFilename();
                if (originalFilename != null && !originalFilename.isBlank()) {
                    fileName = originalFilename;
                }
            }

            DisputeTicket ticket = disputeService.createDispute(userId, category, title, description, evidenceText, fileName);
            redirectAttributes.addFlashAttribute("successMessage", "Dispute ticket " + ticket.getTicketCode() + " created and submitted successfully!");
            return "redirect:/dispute/list?userId=" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit dispute: " + e.getMessage());
            return "redirect:/dispute/form?userId=" + userId;
        }
    }

    /**
     * User cancellation/deletion of their own ticket after submission.
     */
    @PostMapping("/dispute/delete")
    public String deleteDisputeByUser(@RequestParam("ticketId") Long ticketId,
                                      @RequestParam(value = "userId", required = false, defaultValue = "1") Long userId,
                                      RedirectAttributes redirectAttributes) {
        try {
            DisputeTicket ticket = disputeService.getDisputeById(ticketId).orElse(null);
            String ticketCode = (ticket != null) ? ticket.getTicketCode() : ("#" + ticketId);

            disputeService.deleteDisputeByVoter(ticketId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Dispute ticket " + ticketCode + " has been cancelled and deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete dispute ticket: " + e.getMessage());
        }
        return "redirect:/dispute/list?userId=" + userId;
    }

    @GetMapping("/dispute/detail/{id}")
    public String viewDisputeDetail(@PathVariable("id") Long id,
                                    @RequestParam(value = "role", required = false, defaultValue = "VOTER") String role,
                                    Model model) {
        DisputeTicket ticket = disputeService.getDisputeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + id));

        String voterUsername = ticket.getUser().getUsername();
        List<SystemActivityLog> logs = systemLogService.getLogsForVoter(voterUsername);

        model.addAttribute("ticket", ticket);
        model.addAttribute("systemLogs", logs);
        model.addAttribute("role", role);
        return "disputeDetail";
    }

    @PostMapping("/dispute/{id}/evidence")
    public String submitAdditionalEvidence(@PathVariable("id") Long id,
                                           @RequestParam("userId") Long userId,
                                           @RequestParam("additionalEvidence") String additionalEvidence,
                                           RedirectAttributes redirectAttributes) {
        try {
            disputeService.submitAdditionalEvidence(id, userId, additionalEvidence);
            redirectAttributes.addFlashAttribute("successMessage", "Supplemental evidence dispatched to Compliance Auditor.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit evidence: " + e.getMessage());
        }
        return "redirect:/dispute/detail/" + id + "?role=VOTER";
    }

    @PostMapping("/dispute/{id}/acknowledge")
    public String acknowledgeResolution(@PathVariable("id") Long id,
                                        @RequestParam("userId") Long userId,
                                        RedirectAttributes redirectAttributes) {
        try {
            disputeService.acknowledgeResolution(id, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Resolution acknowledged. Dispute is officially closed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to acknowledge resolution: " + e.getMessage());
        }
        return "redirect:/dispute/list?userId=" + userId;
    }

    // ==========================================
    // 2. COMPLIANCE AUDITOR VIEWS
    // ==========================================

    @GetMapping("/auditor/dashboard")
    public String auditorDashboard(@RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                                   Model model) {
        List<DisputeTicket> tickets;
        if ("ALL".equalsIgnoreCase(status)) {
            tickets = disputeService.getAllDisputes();
        } else {
            try {
                TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                tickets = disputeService.getDisputesByStatus(ticketStatus);
            } catch (Exception e) {
                tickets = disputeService.getAllDisputes();
            }
        }

        model.addAttribute("tickets", tickets);
        model.addAttribute("metrics", disputeService.getMetrics());
        model.addAttribute("currentStatus", status.toUpperCase());
        return "auditorDashboard";
    }

    @PostMapping("/auditor/dispute/investigate")
    public String escalateToInvestigation(@RequestParam("ticketId") Long ticketId,
                                          @RequestParam(value = "auditorId", required = false, defaultValue = "3") Long auditorId,
                                          @RequestParam("requestNotes") String requestNotes,
                                          RedirectAttributes redirectAttributes) {
        try {
            disputeService.escalateToUnderInvestigation(ticketId, auditorId, requestNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated to 'Under Investigation'. Notification dispatched to voter.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to escalate dispute: " + e.getMessage());
        }
        return "redirect:/auditor/dashboard";
    }

    @PostMapping("/auditor/dispute/resolve")
    public String resolveAndArchive(@RequestParam("ticketId") Long ticketId,
                                    @RequestParam(value = "auditorId", required = false, defaultValue = "3") Long auditorId,
                                    @RequestParam("findings") String findings,
                                    @RequestParam("actionsTaken") String actionsTaken,
                                    @RequestParam("finalRemarks") String finalRemarks,
                                    RedirectAttributes redirectAttributes) {
        try {
            disputeService.resolveAndArchive(ticketId, auditorId, findings, actionsTaken, finalRemarks);
            redirectAttributes.addFlashAttribute("successMessage", "Dispute Managed and Archived Successfully into Immutable Transparency Log.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to resolve and archive dispute: " + e.getMessage());
        }
        return "redirect:/auditor/dashboard";
    }

    /**
     * Auditor permanently deleting archived/resolved tickets.
     */
    @PostMapping("/auditor/dispute/delete")
    public String deleteDisputeByAuditor(@RequestParam("ticketId") Long ticketId,
                                         @RequestParam(value = "auditorId", required = false, defaultValue = "3") Long auditorId,
                                         RedirectAttributes redirectAttributes) {
        try {
            DisputeTicket ticket = disputeService.getDisputeById(ticketId).orElse(null);
            String ticketCode = (ticket != null) ? ticket.getTicketCode() : ("#" + ticketId);

            disputeService.deleteDisputeByAuditor(ticketId, auditorId);
            redirectAttributes.addFlashAttribute("successMessage", "Archived dispute record " + ticketCode + " has been permanently deleted from records.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete archived record: " + e.getMessage());
        }
        return "redirect:/auditor/dashboard?status=RESOLVED";
    }

    // ==========================================
    // 3. TRANSPARENCY LEDGER VIEW
    // ==========================================

    @GetMapping("/dispute/logs")
    public String viewTransparencyLogs(Model model) {
        List<AuditTransparencyLog> logs = auditLogService.getAllAuditLogs();
        model.addAttribute("logs", logs);
        return "transparencyLog";
    }
}
