package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * MODULE 6 - pages for every user.
 *
 *  /policy/privacy, /policy/terms, /policy/view/{id} : read the documents (public, linked from the sign-up form)
 *  /privacy/consent                                  : "please accept" page (new user or updated policy)
 *  /privacy/voting                                   : consent checkbox of the voting page (consent_type VOTING)
 *  /privacy/my                                       : My consent history + withdrawal / data deletion requests
 */
@Controller
public class PolicyPageController {

    private final PolicyService policyService;
    private final ConsentService consentService;
    private final DeletionRequestService requestService;
    private final PolicyAccounts accounts;

    public PolicyPageController(PolicyService policyService, ConsentService consentService,
                                DeletionRequestService requestService, PolicyAccounts accounts) {
        this.policyService = policyService;
        this.consentService = consentService;
        this.requestService = requestService;
        this.accounts = accounts;
    }

    /** Data for the Module 6 top bar on every page of this controller. */
    @ModelAttribute
    public void topBar(Model model) {
        model.addAttribute("pmAccount", accounts.current().orElse(null));
        model.addAttribute("pmStaff", accounts.isStaff());
        model.addAttribute("pmLegal", accounts.isLegalOfficer());
    }

    // =====================================================================
    // Public documents ("Read" - viewing privacy terms)
    // =====================================================================

    @GetMapping("/policy/privacy")
    public String privacyPolicy(Model model, HttpServletResponse response) {
        return show(policyService.current(PolicyType.PRIVACY_POLICY).orElse(null), PolicyType.PRIVACY_POLICY, model, response);
    }

    @GetMapping("/policy/terms")
    public String terms(Model model, HttpServletResponse response) {
        return show(policyService.current(PolicyType.TERMS_AND_CONDITIONS).orElse(null), PolicyType.TERMS_AND_CONDITIONS, model, response);
    }

    /** One exact version - e.g. the version a user agreed to. Drafts are only shown to the legal staff. */
    @GetMapping("/policy/view/{id}")
    public String viewVersion(@PathVariable("id") Long id, Model model, HttpServletResponse response) {
        PolicyDocument doc = policyService.find(id).orElse(null);
        if (doc != null && doc.isDraft() && !accounts.isStaff()) {
            doc = null;
        }
        return show(doc, doc == null ? null : doc.getPolicyType(), model, response);
    }

    private String show(PolicyDocument doc, PolicyType type, Model model, HttpServletResponse response) {
        if (doc == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        model.addAttribute("doc", doc);
        model.addAttribute("policyType", type);
        if (type != null) {
            model.addAttribute("current", policyService.current(type).orElse(null));
            model.addAttribute("versions", policyService.publishedVersions(type));
        }
        accounts.current().ifPresent(user -> model.addAttribute("myConsents", consentService.currentConsents(user.getId())));
        return "policy/policyView";
    }

    // =====================================================================
    // "Please accept" page - consent is required before the user can continue
    // =====================================================================

    @GetMapping("/privacy/consent")
    public String consentPage(@RequestParam(value = "next", required = false) String next, Model model) {
        User user = accounts.require();
        String safeNext = safeNext(next);
        List<PolicyDocument> mandatory = consentService.mustAccept(user.getId());
        List<PolicyDocument> toAccept = mandatory.isEmpty() ? consentService.optionalUpdates(user.getId()) : mandatory;
        if (toAccept.isEmpty()) {
            return "redirect:" + safeNext; // nothing to accept
        }
        model.addAttribute("toAccept", toAccept);
        model.addAttribute("mandatory", !mandatory.isEmpty());
        model.addAttribute("accepted", consentService.currentConsents(user.getId()));
        model.addAttribute("next", safeNext);
        return "policy/privacyConsent";
    }

    /** "Create" - saves the consent (one consent_records row per document). */
    @PostMapping("/privacy/consent")
    public String acceptPolicies(@RequestParam(value = "agree", required = false) String agree,
                                 @RequestParam(value = "policyId", required = false) List<Long> policyIds,
                                 @RequestParam(value = "next", required = false) String next,
                                 HttpServletRequest request, RedirectAttributes redirect) {
        User user = accounts.require();
        String safeNext = safeNext(next);
        String back = "redirect:/privacy/consent?next=" + PolicyHtml.urlEncode(safeNext);
        if (!"yes".equals(agree)) {
            redirect.addFlashAttribute("pmError", "Please tick the box to confirm that you have read and agree to the documents.");
            return back;
        }
        // only the versions the user actually saw, and only while they are still the current ones
        Set<PolicyType> types = EnumSet.noneOf(PolicyType.class);
        if (policyIds != null) {
            for (Long id : policyIds) {
                PolicyDocument doc = policyService.find(id).orElse(null);
                if (doc == null || !doc.isCurrent()) {
                    redirect.addFlashAttribute("pmError", "A document was updated a moment ago. Please read the latest version.");
                    return back;
                }
                types.add(doc.getPolicyType());
            }
        }
        if (types.isEmpty()) {
            return "redirect:" + safeNext;
        }
        consentService.recordConsent(user.getId(), ConsentType.SIGN_IN, types,
                PolicyAccounts.clientIp(request), PolicyAccounts.userAgent(request));
        redirect.addFlashAttribute("pmSuccess", "Thank you - your consent was saved.");
        return "redirect:" + safeNext;
    }

    // =====================================================================
    // Voting page consent (consent_type = VOTING)
    // The voting pages (/voter/**) send voters here first - see PolicyConsentGate.
    // =====================================================================

    @GetMapping("/privacy/voting")
    public String votingConsentPage(@RequestParam(value = "next", required = false) String next, Model model) {
        User user = accounts.require();
        boolean hasNext = next != null && !next.isBlank();
        List<PolicyDocument> missing = consentService.missingVotingConsent(user.getId());
        if (missing.isEmpty() && hasNext) {
            return "redirect:" + safeNext(next); // already agreed on the voting page - straight on to voting
        }
        model.addAttribute("missing", missing);
        model.addAttribute("currentDocs", policyService.currentPolicies());
        model.addAttribute("votingConsents", consentService.currentConsents(user.getId(), ConsentType.VOTING));
        model.addAttribute("next", hasNext ? safeNext(next) : "");
        return "policy/privacyVoting";
    }

    /** "Create" - saves a VOTING consent record for each document. */
    @PostMapping("/privacy/voting")
    public String giveVotingConsent(@RequestParam(value = "agree", required = false) String agree,
                                    @RequestParam(value = "policyId", required = false) List<Long> policyIds,
                                    @RequestParam(value = "next", required = false) String next,
                                    HttpServletRequest request, RedirectAttributes redirect) {
        User user = accounts.require();
        boolean hasNext = next != null && !next.isBlank();
        String back = "redirect:/privacy/voting" + (hasNext ? "?next=" + PolicyHtml.urlEncode(safeNext(next)) : "");
        if (!"yes".equals(agree)) {
            redirect.addFlashAttribute("pmError", "Please tick the box to give your consent for voting.");
            return back;
        }
        if (policyIds != null) {
            for (Long id : policyIds) {
                PolicyDocument doc = policyService.find(id).orElse(null);
                if (doc == null || !doc.isCurrent()) {
                    redirect.addFlashAttribute("pmError", "A document was updated a moment ago. Please read the latest version.");
                    return back;
                }
            }
        }
        try {
            consentService.recordConsent(user.getId(), ConsentType.VOTING, null,
                    PolicyAccounts.clientIp(request), PolicyAccounts.userAgent(request));
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
            return back;
        }
        redirect.addFlashAttribute("pmSuccess", "Your voting consent was saved.");
        return hasNext ? "redirect:" + safeNext(next) : "redirect:/privacy/voting";
    }

    // =====================================================================
    // My privacy & consent ("Read" history + "Delete" requests)
    // =====================================================================

    @GetMapping("/privacy/my")
    public String myPrivacy(Model model) {
        User user = accounts.require();
        Long id = user.getId();
        model.addAttribute("history", consentService.history(id));
        model.addAttribute("currentDocs", policyService.currentPolicies());
        model.addAttribute("currentConsents", consentService.currentConsents(id));
        model.addAttribute("mustAccept", consentService.mustAccept(id));
        model.addAttribute("optionalUpdates", consentService.optionalUpdates(id));
        model.addAttribute("votingMissing", consentService.missingVotingConsent(id));
        model.addAttribute("votingConsents", consentService.currentConsents(id, ConsentType.VOTING));
        model.addAttribute("requests", requestService.forUser(id));
        model.addAttribute("pendingTypes", requestService.pendingTypes(id));
        return "policy/privacyMy";
    }

    @PostMapping("/privacy/requests")
    public String submitRequest(@RequestParam(value = "requestType", required = false) String requestType,
                                @RequestParam(value = "reason", required = false) String reason,
                                @RequestParam(value = "confirm", required = false) String confirm,
                                RedirectAttributes redirect) {
        User user = accounts.require();
        DeletionRequestType type = DeletionRequestType.parse(requestType);
        try {
            if (type == null) {
                throw new IllegalArgumentException("Choose what you would like us to do.");
            }
            if (type == DeletionRequestType.DELETE_MY_DATA && !"yes".equals(confirm)) {
                throw new IllegalArgumentException("Please tick the box to confirm that you understand your account will be closed.");
            }
            DeletionRequest saved = requestService.submit(user.getId(), type, reason);
            redirect.addFlashAttribute("pmSuccess", "Request " + saved.getCode() + " was sent to the Legal & Governance Officer.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
        }
        return "redirect:/privacy/my#requests";
    }

    /** The user deletes their own request while it is still pending. */
    @PostMapping("/privacy/requests/{id}/cancel")
    public String cancelRequest(@PathVariable("id") Long id, RedirectAttributes redirect) {
        User user = accounts.require();
        try {
            DeletionRequest cancelled = requestService.cancel(id, user.getId());
            redirect.addFlashAttribute("pmSuccess", "Request " + cancelled.getCode() + " was cancelled and deleted.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("pmError", e.getMessage());
        }
        return "redirect:/privacy/my#requests";
    }

    /** Only simple same-site paths may be used as "next" (no redirects to other sites, no loops). */
    static String safeNext(String next) {
        if (next == null || !next.matches("/[A-Za-z0-9/_.?=&%#-]*") || next.startsWith("//")
                || next.startsWith("/privacy/consent") || next.startsWith("/privacy/voting")) {
            return "/dashboard";
        }
        return next;
    }
}
