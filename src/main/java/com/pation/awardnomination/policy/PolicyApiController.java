package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MODULE 6 - REST API (the endpoint table of the specification, under the system's /api prefix).
 *
 *  POST /api/consent                 save a consent record for the signed-in user  {"consentType":"VOTING","agreed":true}
 *  GET  /api/consent/{userId}        a user's consent history (own history, or any user for Legal Officer / Admin)
 *  PUT  /api/policy/{id}             update a policy (Legal & Governance Officer only)
 *  POST /api/deletion-request        create a withdrawal / data deletion request  {"requestType":"DELETE_MY_DATA","reason":"..."}
 *  PUT  /api/deletion-request/{id}   approve or reject  {"decision":"APPROVED","note":"..."}
 *  GET  /api/policy/current          current version of each document (public)
 *
 * The JSP pages use the same services, so both paths read and write the same SQL Server tables.
 */
@RestController
@RequestMapping("/api")
public class PolicyApiController {

    private final PolicyService policyService;
    private final ConsentService consentService;
    private final DeletionRequestService requestService;
    private final PolicyAccounts accounts;

    public PolicyApiController(PolicyService policyService, ConsentService consentService,
                               DeletionRequestService requestService, PolicyAccounts accounts) {
        this.policyService = policyService;
        this.consentService = consentService;
        this.requestService = requestService;
        this.accounts = accounts;
    }

    // ---------------- request bodies ----------------

    public static class ConsentBody {
        private String consentType;
        private Boolean agreed;

        public String getConsentType() { return consentType; }
        public void setConsentType(String consentType) { this.consentType = consentType; }
        public Boolean getAgreed() { return agreed; }
        public void setAgreed(Boolean agreed) { this.agreed = agreed; }
    }

    public static class PolicyBody {
        private String title;
        private String content;
        private String changeSummary;
        private Boolean publish;
        private Boolean majorChange;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getChangeSummary() { return changeSummary; }
        public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }
        public Boolean getPublish() { return publish; }
        public void setPublish(Boolean publish) { this.publish = publish; }
        public Boolean getMajorChange() { return majorChange; }
        public void setMajorChange(Boolean majorChange) { this.majorChange = majorChange; }
    }

    public static class DeletionBody {
        private String requestType;
        private String reason;

        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ReviewBody {
        private String decision;
        private String note;

        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    // ---------------- endpoints ----------------

    @GetMapping("/policy/current")
    public Map<String, Object> currentPolicies() {
        Map<String, Object> body = new LinkedHashMap<>();
        for (PolicyType type : PolicyType.values()) {
            body.put(type.name(), policyService.current(type).map(doc -> policyJson(doc, false)).orElse(null));
        }
        return body;
    }

    @PostMapping("/consent")
    public ResponseEntity<Map<String, Object>> recordConsent(@RequestBody(required = false) ConsentBody body,
                                                             HttpServletRequest request) {
        Optional<User> user = accounts.current();
        if (user.isEmpty()) {
            return error(401, "Please sign in first.");
        }
        ConsentType type = body == null ? null : ConsentType.parse(body.getConsentType());
        if (type == null) {
            return error(400, "consentType must be REGISTRATION, VOTING or SIGN_IN.");
        }
        if (!Boolean.TRUE.equals(body.getAgreed())) {
            return error(400, "The user has to tick \"I agree\" (agreed: true) before consent can be saved.");
        }
        try {
            List<ConsentRecord> saved = consentService.recordConsent(user.get().getId(), type, null,
                    PolicyAccounts.clientIp(request), PolicyAccounts.userAgent(request));
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", "Consent saved.");
            result.put("records", consentJson(saved));
            return ResponseEntity.status(201).body(result);
        } catch (IllegalStateException e) {
            return error(409, e.getMessage());
        }
    }

    @GetMapping("/consent/{userId}")
    public ResponseEntity<Map<String, Object>> consentHistory(@PathVariable("userId") Long userId) {
        Optional<User> me = accounts.current();
        if (me.isEmpty()) {
            return error(401, "Please sign in first.");
        }
        if (!me.get().getId().equals(userId) && !accounts.isStaff()) {
            return error(403, "You can only read your own consent history.");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("records", consentJson(consentService.history(userId)));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/policy/{id}")
    public ResponseEntity<Map<String, Object>> updatePolicy(@PathVariable("id") Long id,
                                                            @RequestBody(required = false) PolicyBody body) {
        if (!accounts.isLegalOfficer()) {
            return error(403, "Only the Legal & Governance Officer can change policies.");
        }
        if (body == null) {
            return error(400, "Send title, content and changeSummary as JSON.");
        }
        Optional<PolicyDocument> target = policyService.find(id);
        if (target.isEmpty()) {
            return error(404, "Policy version not found.");
        }
        String editor = accounts.require().getUsername();
        try {
            // a published version is never changed: the update goes into a (new or open) draft of the same document
            PolicyDocument draft = target.get().isDraft() ? target.get()
                    : policyService.createDraft(target.get().getPolicyType(), editor);
            String title = body.getTitle() != null ? body.getTitle() : draft.getTitle();
            String content = body.getContent() != null ? body.getContent() : draft.getContent();
            String summary = body.getChangeSummary() != null ? body.getChangeSummary() : draft.getChangeSummary();
            PolicyDocument saved = policyService.saveDraft(draft.getId(), title, content, summary, editor);
            if (Boolean.TRUE.equals(body.getPublish())) {
                saved = policyService.publish(saved.getId(), Boolean.TRUE.equals(body.getMajorChange()), editor);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", saved.isDraft() ? "Draft saved." : "Published as version " + saved.getVersionLabel() + ".");
            result.put("policy", policyJson(saved, true));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(400, e.getMessage());
        }
    }

    @PostMapping("/deletion-request")
    public ResponseEntity<Map<String, Object>> createRequest(@RequestBody(required = false) DeletionBody body) {
        Optional<User> user = accounts.current();
        if (user.isEmpty()) {
            return error(401, "Please sign in first.");
        }
        DeletionRequestType type = body == null ? null : DeletionRequestType.parse(body.getRequestType());
        if (type == null) {
            return error(400, "requestType must be WITHDRAW_CONSENT or DELETE_MY_DATA.");
        }
        try {
            DeletionRequest saved = requestService.submit(user.get().getId(), type, body.getReason());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", "Request " + saved.getCode() + " created.");
            result.put("request", requestJson(saved));
            return ResponseEntity.status(201).body(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(409, e.getMessage());
        }
    }

    @PutMapping("/deletion-request/{id}")
    public ResponseEntity<Map<String, Object>> reviewRequest(@PathVariable("id") Long id,
                                                             @RequestBody(required = false) ReviewBody body) {
        if (!accounts.isStaff()) {
            return error(403, "Only the Legal & Governance Officer or the System Administrator can review requests.");
        }
        RequestStatus decision = body == null ? null : RequestStatus.parse(body.getDecision());
        if (decision != RequestStatus.APPROVED && decision != RequestStatus.REJECTED) {
            return error(400, "decision must be APPROVED or REJECTED.");
        }
        User me = accounts.require();
        try {
            DeletionRequest done = decision == RequestStatus.APPROVED
                    ? requestService.approve(id, me.getId(), me.getUsername(), body.getNote())
                    : requestService.reject(id, me.getId(), me.getUsername(), body.getNote());
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", "Request " + done.getCode() + " " + done.getStatus().getLabel().toLowerCase() + ".");
            result.put("request", requestJson(done));
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(400, e.getMessage());
        }
    }

    // ---------------- JSON helpers (plain maps, dates as text) ----------------

    private static ResponseEntity<Map<String, Object>> error(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private static List<Map<String, Object>> consentJson(List<ConsentRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ConsentRecord r : records) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("code", r.getCode());
            m.put("userId", r.getUserId());
            m.put("policyId", r.getPolicyId());
            m.put("policyType", r.getPolicyType().name());
            m.put("policyVersion", r.getPolicyVersion());
            m.put("consentType", r.getConsentType().name());
            m.put("agreedAt", String.valueOf(r.getAgreedAt()));
            m.put("status", r.getStatus().name());
            m.put("withdrawnAt", r.getWithdrawnAt() == null ? null : r.getWithdrawnAt().toString());
            list.add(m);
        }
        return list;
    }

    private static Map<String, Object> policyJson(PolicyDocument doc, boolean withContent) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", doc.getId());
        m.put("policyType", doc.getPolicyType().name());
        m.put("title", doc.getTitle());
        m.put("version", doc.getVersionLabel());
        m.put("status", doc.getStatus().name());
        m.put("requiresReacceptance", doc.isRequiresReacceptance());
        m.put("changeSummary", doc.getChangeSummary());
        m.put("publishedAt", doc.getPublishedAt() == null ? null : doc.getPublishedAt().toString());
        m.put("url", doc.isDraft() ? null : "/policy/view/" + doc.getId());
        if (withContent) {
            m.put("content", doc.getContent());
        }
        return m;
    }

    private static Map<String, Object> requestJson(DeletionRequest r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("code", r.getCode());
        m.put("userId", r.getUserId());
        m.put("requestType", r.getRequestType().name());
        m.put("reason", r.getReason());
        m.put("status", r.getStatus().name());
        m.put("requestedAt", String.valueOf(r.getRequestedAt()));
        m.put("reviewedBy", r.getReviewedBy());
        m.put("reviewedAt", r.getReviewedAt() == null ? null : r.getReviewedAt().toString());
        m.put("reviewNote", r.getReviewNote());
        m.put("outcome", r.getOutcome());
        return m;
    }
}
