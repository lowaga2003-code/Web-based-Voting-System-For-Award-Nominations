package com.pation.awardnomination.dashboard;

import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.admin.UserRepository;
import com.pation.awardnomination.nominee.Interview;
import com.pation.awardnomination.nominee.InterviewRepository;
import com.pation.awardnomination.nominee.Nominee;
import com.pation.awardnomination.nominee.NomineeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [INTEGRATION] Main dashboard of the whole Award Nomination System.
 *
 * (සිංහලෙන්: Main login එකෙන් log වුණාම හැමෝම එන්නේ මෙතනට. Log වුණ role එක අනුව
 *  පේන module cards වෙනස් වෙනවා. Admin ට හැම module එකක්ම සහ system stats පේනවා.)
 *
 * Only READS from the admin and nominee repositories - it does not change their code.
 */
@Controller
public class DashboardController {

    static final String ADMIN_ROLE = "ROLE_SYSTEM_ADMINISTRATOR";

    private final UserRepository userRepository;
    private final NomineeRepository nomineeRepository;
    private final InterviewRepository interviewRepository;

    public DashboardController(UserRepository userRepository,
                               NomineeRepository nomineeRepository,
                               InterviewRepository interviewRepository) {
        this.userRepository = userRepository;
        this.nomineeRepository = nomineeRepository;
        this.interviewRepository = interviewRepository;
    }

    /** Site root -> dashboard (Spring Security sends guests to /login first). */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Set<String> roles = new LinkedHashSet<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        boolean isAdmin = roles.contains(ADMIN_ROLE);

        String email = auth.getName(); // main login uses the email as the username
        String displayName = userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElse(email);

        model.addAttribute("displayName", displayName);
        model.addAttribute("email", email);
        model.addAttribute("roleLabel", roleLabels(roles));
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("modules", buildModules(roles, isAdmin));

        if (isAdmin) {
            addAdminStats(model);
        }
        return "dashboard";
    }

    /**
     * Friendly 403 page. Spring Security forwards here with the ORIGINAL http method,
     * so page requests (GET/POST) get the JSP ...
     */
    @RequestMapping(value = "/access-denied", method = {RequestMethod.GET, RequestMethod.POST})
    public String accessDenied(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            Set<String> roles = new LinkedHashSet<>();
            for (GrantedAuthority authority : auth.getAuthorities()) {
                roles.add(authority.getAuthority());
            }
            model.addAttribute("roleLabel", roleLabels(roles));
        }
        return "access-denied";
    }

    /** ... and denied REST calls (PUT/DELETE/PATCH) get a JSON 403, because JSP pages only render for GET/POST. */
    @RequestMapping(value = "/access-denied", method = {RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<Map<String, String>> accessDeniedApi() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
    }

    // ---------------------------------------------------------------------------------------
    // Module cards. To connect another member's part later: give its card a Link list.
    // ---------------------------------------------------------------------------------------
    private List<DashboardModule> buildModules(Set<String> roles, boolean isAdmin) {
        List<DashboardModule> all = new ArrayList<>();

        // Module 1 - admin member's part (CONNECTED)
        all.add(new DashboardModule("users", "Module 1", "User Account & Role Management", "System Administrator",
                "Create and verify accounts, change roles, suspend or remove users. Runs the secure login for the whole system.",
                ADMIN_ROLE,
                List.of(new DashboardModule.Link("Manage users", "/admin/users"))));

        // Module 2 - Award Category Management (CONNECTED in v5). The Programme Manager (and admin) get the
        // control board; every other user sees the public award calendar (the deadlines all modules follow).
        boolean categoryStaff = isAdmin || roles.contains("ROLE_PROGRAM_MANAGER");
        List<DashboardModule.Link> categoryLinks = categoryStaff
                ? List.of(new DashboardModule.Link("Control board", "/manager/awards"),
                          new DashboardModule.Link("Live statistics", "/manager/awards/stats"),
                          new DashboardModule.Link("Award calendar", "/timeline"))
                : List.of(new DashboardModule.Link("Award calendar", "/timeline"));
        all.add(new DashboardModule("category", "Module 2", "Award Category Management", "Programme Manager",
                "Award categories, eligibility rules, and the nomination and voting deadlines that drive the whole season.",
                "ROLE_PROGRAM_MANAGER",
                categoryLinks,
                true));

        // Module 3 - Nominee module (CONNECTED). Admins go straight to the nominee admin pages.
        List<DashboardModule.Link> nomineeLinks = isAdmin
                ? List.of(new DashboardModule.Link("Nominee dashboard", "/nominee/list"),
                          new DashboardModule.Link("Interview slots", "/nominee/interviews"))
                : List.of(new DashboardModule.Link("Open my nomination", "/nominee/home"));
        all.add(new DashboardModule("nominee", "Module 3", "Nominee Profile & Evidence", "Nominee",
                "Nominee profiles, evidence uploads, status tracking (Draft, Under Review, Approved) and interview booking.",
                "ROLE_NOMINEE",
                nomineeLinks));

        // Module 4 - Audit & Dispute (CONNECTED). Auditors/admin get the auditor hub; every other user can raise disputes.
        boolean auditStaff = isAdmin || roles.contains("ROLE_COMPLIANCE_AUDITOR");
        List<DashboardModule.Link> auditLinks = auditStaff
                ? List.of(new DashboardModule.Link("Auditor hub", "/auditor/dashboard"),
                          new DashboardModule.Link("Transparency ledger", "/dispute/logs"))
                : List.of(new DashboardModule.Link("My disputes", "/dispute/list"),
                          new DashboardModule.Link("Transparency ledger", "/dispute/logs"));
        all.add(new DashboardModule("audit", "Module 4", "Audit & Dispute Management", "Compliance Auditor",
                "Dispute tickets from registered users, auditor investigation, and the tamper-proof transparency ledger.",
                "ROLE_COMPLIANCE_AUDITOR",
                auditLinks,
                true));

        // Module 5 - Evaluation & Selection (CONNECTED): Selection Committee Chairperson (+ admin)
        all.add(new DashboardModule("evaluation", "Module 5", "Evaluation & Selection", "Selection Committee Chairperson",
                "Appoint judges, scoring rubrics, shortlist review and final winners.",
                "ROLE_SELECTION_COMMITTEE_CHAIRPERSON",
                List.of(new DashboardModule.Link("Selection committee", "/evaluation"),
                        new DashboardModule.Link("Results & winners", "/evaluation/results"))));

        // Module 6 - Policy & Consent (CONNECTED). Legal Officer / admin get the legal console;
        // every other user gets their own consent history and the policy documents.
        boolean legalStaff = isAdmin || roles.contains("ROLE_LEGAL_GOVERNANCE_OFFICER");
        List<DashboardModule.Link> policyLinks = legalStaff
                ? List.of(new DashboardModule.Link("Legal console", "/legal/dashboard"),
                          new DashboardModule.Link("Deletion requests", "/legal/requests"))
                : List.of(new DashboardModule.Link("My privacy & consent", "/privacy/my"),
                          new DashboardModule.Link("Privacy policy", "/policy/privacy"));
        all.add(new DashboardModule("policy", "Module 6", "Policy & Consent Management", "Legal & Governance Officer",
                "Privacy Policy and Terms versions, timestamped consent records, and data deletion requests.",
                "ROLE_LEGAL_GOVERNANCE_OFFICER",
                policyLinks,
                true));

        // Voting (CONNECTED in v5) - registered users vote; the admin can watch the live counts in Module 2.
        List<DashboardModule.Link> votingLinks = roles.contains("ROLE_VOTER")
                ? List.of(new DashboardModule.Link("My ballot", "/voter/ballot"),
                          new DashboardModule.Link("My receipts", "/voter/ballot/history"),
                          new DashboardModule.Link("Results", "/voter/ballot/results"))
                : List.of(new DashboardModule.Link("Live vote counts", "/manager/awards/stats"));
        all.add(new DashboardModule("voting", "Voting", "Cast your vote", "Registered User (Voter)",
                "One vote per award category, protected by the voter's 2-step (OTP) login. Every ballot is sealed in the transparency log.",
                "ROLE_VOTER",
                votingLinks));

        // Admin sees every module; everyone else sees the module(s) of their own role + modules open to everyone.
        // [INTEGRATION v5] the user's own part comes first (e.g. Voting for a voter), shared modules after it.
        List<DashboardModule> visible = new ArrayList<>();
        for (DashboardModule module : all) {
            if (isAdmin || roles.contains(module.getOwnerRole())) {
                visible.add(module);
            }
        }
        for (DashboardModule module : all) {
            if (module.isForEveryone() && !visible.contains(module)) {
                visible.add(module);
            }
        }
        return visible;
    }

    private void addAdminStats(Model model) {
        List<User> users = userRepository.findAll();
        long activeUsers = users.stream().filter(User::isActive).count();

        List<Nominee> nominees = nomineeRepository.findAll();
        long approved = nominees.stream().filter(n -> "Approved".equals(n.getStatus())).count();
        long underReview = nominees.stream().filter(n -> "Under Review".equals(n.getStatus())).count();
        long drafts = nominees.stream().filter(n -> "Draft".equals(n.getStatus())).count();

        List<Interview> interviews = interviewRepository.findAll();
        long booked = interviews.stream().filter(i -> "Booked".equals(i.getStatus())).count();

        model.addAttribute("totalUsers", (long) users.size());
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("totalNominees", (long) nominees.size());
        model.addAttribute("approvedNominees", approved);
        model.addAttribute("reviewNominees", underReview);
        model.addAttribute("draftNominees", drafts);
        model.addAttribute("totalSlots", (long) interviews.size());
        model.addAttribute("bookedSlots", booked);
    }

    private static String roleLabels(Set<String> roles) {
        List<String> labels = new ArrayList<>();
        for (String role : roles) {
            labels.add(roleLabel(role));
        }
        return labels.isEmpty() ? "User" : String.join(", ", labels);
    }

    static String roleLabel(String authority) {
        switch (authority) {
            case "ROLE_SYSTEM_ADMINISTRATOR": return "System Administrator";
            case "ROLE_PROGRAM_MANAGER": return "Programme Manager";
            case "ROLE_NOMINEE": return "Nominee";
            case "ROLE_VOTER": return "Voter";
            case "ROLE_COMPLIANCE_AUDITOR": return "Compliance Auditor";
            case "ROLE_SELECTION_COMMITTEE_CHAIRPERSON": return "Selection Committee Chairperson";
            case "ROLE_LEGAL_GOVERNANCE_OFFICER": return "Legal & Governance Officer";
            default: return authority.startsWith("ROLE_") ? authority.substring(5) : authority;
        }
    }
}
