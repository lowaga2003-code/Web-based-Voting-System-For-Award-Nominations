package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Registered User (Dispute Creator) actions.
 */
@RestController
@RequestMapping("/api/disputes")
@CrossOrigin(origins = "*")
public class DisputeController {

    private final DisputeService disputeService;

    @Autowired
    public DisputeController(DisputeService disputeService) {
        this.disputeService = disputeService;
    }

    public static class CreateDisputeRequest {
        private Long userId;
        private DisputeCategory category;
        private String title;
        private String description;
        private String evidenceText;
        private String evidenceFileName;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public DisputeCategory getCategory() { return category; }
        public void setCategory(DisputeCategory category) { this.category = category; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getEvidenceText() { return evidenceText; }
        public void setEvidenceText(String evidenceText) { this.evidenceText = evidenceText; }
        public String getEvidenceFileName() { return evidenceFileName; }
        public void setEvidenceFileName(String evidenceFileName) { this.evidenceFileName = evidenceFileName; }
    }

    public static class AdditionalEvidenceRequest {
        private Long userId;
        private String additionalEvidence;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getAdditionalEvidence() { return additionalEvidence; }
        public void setAdditionalEvidence(String additionalEvidence) { this.additionalEvidence = additionalEvidence; }
    }

    public static class AcknowledgeRequest {
        private Long userId;
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createTicket(@RequestBody CreateDisputeRequest req) {
        try {
            DisputeTicket ticket = disputeService.createDispute(
                    req.getUserId(),
                    req.getCategory(),
                    req.getTitle(),
                    req.getDescription(),
                    req.getEvidenceText(),
                    req.getEvidenceFileName()
            );
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DisputeTicket>> getUserDisputes(@PathVariable Long userId) {
        return ResponseEntity.ok(disputeService.getDisputesForUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDisputeById(@PathVariable Long id) {
        return disputeService.getDisputeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/additional-evidence")
    public ResponseEntity<?> submitAdditionalEvidence(@PathVariable Long id, @RequestBody AdditionalEvidenceRequest req) {
        try {
            DisputeTicket ticket = disputeService.submitAdditionalEvidence(id, req.getUserId(), req.getAdditionalEvidence());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeResolution(@PathVariable Long id, @RequestBody AcknowledgeRequest req) {
        try {
            DisputeTicket ticket = disputeService.acknowledgeResolution(id, req.getUserId());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<DisputeNotification>> getNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(disputeService.getNotificationsForUser(userId));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id) {
        disputeService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * User cancellation/deletion of their own ticket after submission.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDisputeTicket(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            disputeService.deleteDisputeByVoter(id, userId);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Dispute ticket successfully cancelled and deleted.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }
}
