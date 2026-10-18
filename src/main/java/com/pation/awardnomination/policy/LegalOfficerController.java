package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MODULE 6 - Legal & Governance console (/legal/**).
 *
 * Legal & Governance Officer: everything, including editing / publishing policies.
 * System Administrator: can see consent records and approve / reject deletion requests, but NOT edit policies.
 *
 * Role checks (the "middleware" in the specification) happen three times:
 *  1. SecurityConfig URL rules, 2. @PreAuthorize on the policy-editing methods, 3. accounts.requireXxx() in the code.
 */
@Controller
@RequestMapping("/legal")
public class LegalOfficerController {

    private static final String LEGAL_ONLY = "hasRole('LEGAL_GOVERNANCE_OFFICER')";

    private final PolicyService policyService;
    private final ConsentService consentService;
    private final DeletionRequestService requestService;
    private final PolicyAccounts accounts;

    public LegalOfficerController(PolicyService policyService, ConsentService consentService,
                                  DeletionRequestService requestService, PolicyAccounts accounts) {
        this.policyService = policyService;
        this.consentService = consentService;
        this.requestService = requestService;
        this.accounts = accounts;
    }

    @ModelAttribute
    public void topBar(Model model) {
        model.addAttribute("pmAccount", accounts.current().orElse(null));
        model.addAttribute("pmStaff", accounts.isStaff());
        model.addAttribute("pmLegal", accounts.isLegalOfficer());
    }

    // =====================================================================
    // Overview
    // =====================================================================

    @GetMapping("/dashboard")
    public String overview(Model model) {
        accounts.requireStaff();
        Map<PolicyType, PolicyDocument> current = policyService.currentPolicies();
        Map<PolicyType, PolicyDocument> drafts = new EnumMap<>(PolicyType.class);
        Map<PolicyType, Long> usersOnCurrent = new EnumMap<>(PolicyType.class);
        for (PolicyType type : PolicyType.values()) {
            policyService.draft(type).ifPresent(draft -> drafts.put(type, draft));
            PolicyDocument doc = current.get(type);
            usersOnCurrent.put(type, doc == null ? 0L : consentService.usersOnVersion(doc.getId()));
        }

        List<DeletionRequest> pending = requestService.byStatus(RequestStatus.PENDING);
        List<ConsentRecord> recent = consentService.recent();
        List<Long> userIds = new ArrayList<>();
        for (DeletionRequest r : pending) {
            userIds.add(r.getUserId());
        }
        for (ConsentRecord c : recent) {
            userIds.add(c.getUserId());
        }

        model.addAttribute("current", current);
        model.addAttribute("drafts", drafts);
        model.addAttribute("usersOnCurrent", usersOnCurrent);
        model.addAttribute("pendingRequests", pending.size() > 5 ? pending.subList(0, 5) : pending);
        model.addAttribute("pendingCount", (long) pending.size());
        model.addAttribute("recentConsents", recent.size() > 8 ? recent.subList(0, 8) : recent);
        model.addAttribute("people", accounts.byIds(userIds));
        model.addAttribute("activeConsents", consentService.countByStatus(ConsentStatus.ACTIVE));
        model.addAttribute("withdrawnConsents", consentService.countByStatus(ConsentStatus.WITHDRAWN));
        model.addAttribute("publishedVersions", policyService.countByStatus(PolicyStatus.PUBLISHED)
                + policyService.countByStatus(PolicyStatus.ARCHIVED));
        model.addAttribute("draftCount", policyService.countByStatus(PolicyStatus.DRAFT));

        Map<String, Long> tables = new LinkedHashMap<>();   // live row counts - shows the SQL Server connection
        tables.put("policy_documents", policyService.count());
        tables.put("consent_records", consentService.count());
        tables.put("data_deletion_requests", requestService.count());
        model.addAttribute("tables", tables);
        return "policy/legalDashboard";
    }

    // =====================================================================
    // Policies ("Update")
    // =====================================================================

    @GetMapping("/policies")
    public String policies(Model model) {
        accounts.requireStaff();
        Map<PolicyType, List<PolicyDocument>> versions = new EnumMap<>(PolicyType.class);
        Map<Long, Long> agreed = new HashMap<>();
        for (PolicyType type : PolicyType.values()) {
            List<PolicyDocument> list = policyService.versions(type);
            versions.put(type, list);
            for (PolicyDocument doc : list) {
                if (!doc.isDraft()) {
                    agreed.put(doc.getId(), consentService.usersOnVersion(doc.getId()));
                }
            }
        }
        model.addAttribute("versions", versions);
        model.addAttribute("agreed", agreed);
        return "policy/legalPolicies";
    }

    /** INSERT - opens a draft (a copy of the current text) and goes to the editor. */
    @PreAuthorize(LEGAL_ONLY)
    @PostMapping("/policies/create")
    public String createDraft(@RequestParam(value = "type", required = false) String type, RedirectAttributes redirect) {
        accounts.requireLegalOfficer();
        PolicyType policyType = PolicyType.parse(type);
        if (policyType == null) {
            redirect.addFlashAttribute("pmError", "Unknown document type.");
            return "redirect:/legal/policies";
        }
        PolicyDocument draft = policyService.createDraft(policyType, accounts.require().getUsername());
        return "redirect:/legal/policies/" + draft.getId() + "/edit";
    }

    @PreAuthorize(LEGAL_ONLY)
    @GetMapping("/policies/{id}/edit")
    public String editDraft(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        accounts.requireLegalOfficer();
        PolicyDocument doc = policyService.find(id).orElse(null);
        if (doc == null || !doc.isDraft()) {
            redirect.addFlashAttribute("pmError", "Only drafts can be edited. Published versions never change - open a new draft instead.");
            return "redirect:/legal/policies";
        }
        model.addAttribute("doc", doc);
        model.addAttribute("current", policyService.current(doc.getPolicyType()).orElse(null));
        model.addAttribute("firstVersion", !policyService.hasPublishedVersion(doc.getPolicyType()));
        model.addAttribute("nextMinor", policyService.nextVersionLabel(doc.getPolicyType(), false));
        model.addAttribute("nextMajor", policyService.nextVersionLabel(doc.getPolicyType(), true));
        return "policy/legalPolicyEditor";
    }

    /** UPDATE - saves the draft; with action=publish it also publishes it as a new version. */
    @PreAuthorize(LEGAL_ONLY)
    @PostMapping("/policies/{id}")
    public String saveDraft(@PathVariable("id") Long id,
                            @RequestParam(value = "title", required = false) String title,
                            @RequestParam(value = "content", required = false) String content,
                            @RequestParam(value = "changeSummary", required = false) String changeSummary,
                            @RequestParam(value = "action", required = false, defaultValue = "save") String action,
                            @RequestParam(value = "change", required = false, defaultValue = "minor") String change,
                            RedirectAttributes redirect) {
        accounts.requireLegalOfficer();
        String editor = accounts.require().getUsername();
        try {
            PolicyDocument saved = policyService.saveDraft(id, title, content, changeSummary, editor);
            if ("publish".equals(action)) {
                PolicyDocument published = policyService.publish(saved.getId(), "major".equals(change), editor);
                redirect.addFlashAttribute("pmSuccess", published.getPolicyType().getDisplayName() + " version "
                        + published.getVersionLabel() + " is now live. " + (published.isRequiresReacceptance()
                        ? "Every user will be asked to accept it the next time they open a page."
                        : "Users can see it; they don't have to accept it again."));
                return "redirect:/legal/policies";
            }
            redirect.addFlashAttribute("pmSuccess", "Draft saved.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
        }
        return "redirect:/legal/policies/" + id + "/edit";
    }

    /** DELETE - only drafts. */
    @PreAuthorize(LEGAL_ONLY)
    @PostMapping("/policies/{id}/delete")
    public String deleteDraft(@PathVariable("id") Long id, RedirectAttributes redirect) {
        accounts.requireLegalOfficer();
        try {
            policyService.deleteDraft(id);
            redirect.addFlashAttribute("pmSuccess", "Draft deleted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
        }
        return "redirect:/legal/policies";
    }

    // =====================================================================
    // Consent records ("Read" - which version did a user agree to?)
    // =====================================================================

    @GetMapping("/consents")
    public String consents(@RequestParam(value = "q", required = false) String q, Model model) {
        accounts.requireStaff();
        String query = PolicyHtml.trim(q);
        model.addAttribute("q", query);
        model.addAttribute("activeConsents", consentService.countByStatus(ConsentStatus.ACTIVE));
        model.addAttribute("withdrawnConsents", consentService.countByStatus(ConsentStatus.WITHDRAWN));
        if (!query.isEmpty()) {
            model.addAttribute("searched", true);
            Optional<User> found = accounts.find(query);
            if (found.isPresent()) {
                Long userId = found.get().getId();
                model.addAttribute("found", found.get());
                model.addAttribute("records", consentService.history(userId));
                model.addAttribute("currentConsents", consentService.currentConsents(userId));
                model.addAttribute("mustAccept", consentService.mustAccept(userId));
                model.addAttribute("requests", requestService.forUser(userId));
            }
        } else {
            List<ConsentRecord> recent = consentService.recent();
            List<Long> userIds = new ArrayList<>();
            for (ConsentRecord c : recent) {
                userIds.add(c.getUserId());
            }
            model.addAttribute("records", recent);
            model.addAttribute("people", accounts.byIds(userIds));
        }
        return "policy/legalConsents";
    }

    // =====================================================================
    // Deletion requests (approve / reject)
    // =====================================================================

    @GetMapping("/requests")
    public String requests(@RequestParam(value = "status", required = false, defaultValue = "PENDING") String status,
                           Model model) {
        accounts.requireStaff();
        RequestStatus filter = RequestStatus.parse(status);
        List<DeletionRequest> list = filter == null ? requestService.all() : requestService.byStatus(filter);
        List<Long> userIds = new ArrayList<>();
        for (DeletionRequest r : list) {
            userIds.add(r.getUserId());
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (RequestStatus s : RequestStatus.values()) {
            counts.put(s.name(), requestService.countByStatus(s));
        }
        counts.put("ALL", requestService.count());

        model.addAttribute("filter", filter == null ? "ALL" : filter.name());
        model.addAttribute("requests", list);
        model.addAttribute("counts", counts);
        model.addAttribute("people", accounts.byIds(userIds));
        model.addAttribute("myId", accounts.require().getId());
        return "policy/legalRequests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approve(@PathVariable("id") Long id, @RequestParam(value = "note", required = false) String note,
                          RedirectAttributes redirect) {
        accounts.requireStaff();
        User me = accounts.require();
        try {
            DeletionRequest done = requestService.approve(id, me.getId(), me.getUsername(), note);
            redirect.addFlashAttribute("pmSuccess", "Request " + done.getCode() + " approved. " + done.getOutcome());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
        }
        return "redirect:/legal/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable("id") Long id, @RequestParam(value = "note", required = false) String note,
                         RedirectAttributes redirect) {
        accounts.requireStaff();
        User me = accounts.require();
        try {
            DeletionRequest done = requestService.reject(id, me.getId(), me.getUsername(), note);
            redirect.addFlashAttribute("pmSuccess", "Request " + done.getCode() + " rejected.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
        }
        return "redirect:/legal/requests";
    }
}
