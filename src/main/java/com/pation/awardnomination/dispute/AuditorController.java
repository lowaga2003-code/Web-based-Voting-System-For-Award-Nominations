package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Compliance Auditor actions.
 */
@RestController
@RequestMapping("/api/auditor")
@CrossOrigin(origins = "*")
public class AuditorController {

    private final DisputeService disputeService;
    private final SystemLogService systemLogService;

    @Autowired
    public AuditorController(DisputeService disputeService, SystemLogService systemLogService) {
        this.disputeService = disputeService;
        this.systemLogService = systemLogService;
    }

    public static class InvestigateRequest {
        private Long auditorId;
        private String evidenceRequestNotes;

        public Long getAuditorId() { return auditorId; }
        public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }
        public String getEvidenceRequestNotes() { return evidenceRequestNotes; }
        public void setEvidenceRequestNotes(String evidenceRequestNotes) { this.evidenceRequestNotes = evidenceRequestNotes; }
    }

    public static class ResolveRequest {
        private Long auditorId;
        private String findings;
        private String actionsTaken;
        private String finalRemarks;

        public Long getAuditorId() { return auditorId; }
        public void setAuditorId(Long auditorId) { this.auditorId = auditorId; }
        public String getFindings() { return findings; }
        public void setFindings(String findings) { this.findings = findings; }
        public String getActionsTaken() { return actionsTaken; }
        public void setActionsTaken(String actionsTaken) { this.actionsTaken = actionsTaken; }
        public String getFinalRemarks() { return finalRemarks; }
        public void setFinalRemarks(String finalRemarks) { this.finalRemarks = finalRemarks; }
    }

    @GetMapping("/disputes")
    public ResponseEntity<List<DisputeTicket>> getDisputes(@RequestParam(required = false) String status) {
        if (status == null || status.equalsIgnoreCase("ALL")) {
            return ResponseEntity.ok(disputeService.getAllDisputes());
        }
        try {
            TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(disputeService.getDisputesByStatus(ticketStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(disputeService.getAllDisputes());
        }
    }

    @GetMapping("/disputes/{id}")
    public ResponseEntity<?> getDisputeDetails(@PathVariable Long id) {
        return disputeService.getDisputeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disputes/{id}/system-logs")
    public ResponseEntity<?> getAssociatedSystemLogs(@PathVariable Long id) {
        return disputeService.getDisputeById(id).map(ticket -> {
            String voterUsername = ticket.getUser().getUsername();
            List<SystemActivityLog> logs = systemLogService.getLogsForVoter(voterUsername);
            return ResponseEntity.ok(logs);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/disputes/{id}/investigate")
    public ResponseEntity<?> escalateToUnderInvestigation(@PathVariable Long id, @RequestBody InvestigateRequest req) {
        try {
            DisputeTicket ticket = disputeService.escalateToUnderInvestigation(id, req.getAuditorId(), req.getEvidenceRequestNotes());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ticket status updated to 'Under Investigation'. Notification dispatched to user.");
            response.put("ticket", ticket);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/disputes/{id}/resolve")
    public ResponseEntity<?> resolveAndArchive(@PathVariable Long id, @RequestBody ResolveRequest req) {
        try {
            DisputeTicket ticket = disputeService.resolveAndArchive(
                    id,
                    req.getAuditorId(),
                    req.getFindings(),
                    req.getActionsTaken(),
                    req.getFinalRemarks()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dispute Managed and Archived Successfully");
            response.put("ticket", ticket);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        return ResponseEntity.ok(disputeService.getMetrics());
    }

    /**
     * Auditor permanently deleting archived/resolved tickets.
     */
    @DeleteMapping("/disputes/{id}")
    public ResponseEntity<?> deleteArchivedDispute(@PathVariable Long id, @RequestParam(required = false) Long auditorId) {
        try {
            disputeService.deleteDisputeByAuditor(id, auditorId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archived dispute record successfully deleted.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
