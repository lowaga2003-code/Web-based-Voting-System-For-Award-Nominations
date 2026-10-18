package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.Role;
import com.pation.awardnomination.admin.RoleName;
import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.admin.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MODULE 6 - who is signed in, and which role checks apply.
 *
 * Module 6 uses the main login accounts (Module 1, table "users"), so consent records and
 * deletion requests store users.id. Only READS Module 1's repository.
 */
@Component
public class PolicyAccounts {

    public static final String LEGAL_OFFICER = "ROLE_LEGAL_GOVERNANCE_OFFICER";
    public static final String ADMIN = "ROLE_SYSTEM_ADMINISTRATOR";

    private final UserRepository users;

    public PolicyAccounts(UserRepository users) {
        this.users = users;
    }

    /** The signed-in main-login account (the login uses the e-mail address as the user name). */
    public Optional<User> current() {
        Authentication auth = authentication();
        if (auth == null) {
            return Optional.empty();
        }
        return users.findByEmail(auth.getName());
    }

    public User require() {
        return current().orElseThrow(() -> new AccessDeniedException("Please sign in first."));
    }

    public boolean hasRole(String role) {
        Authentication auth = authentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public boolean isLegalOfficer() { return hasRole(LEGAL_OFFICER); }

    public boolean isAdmin() { return hasRole(ADMIN); }

    /** Legal & Governance Officer or System Administrator. */
    public boolean isStaff() { return isLegalOfficer() || isAdmin(); }

    /** Role check from the specification: only the Legal & Governance Officer may change policies. */
    public void requireLegalOfficer() {
        if (!isLegalOfficer()) {
            throw new AccessDeniedException("Only the Legal & Governance Officer can change policies.");
        }
    }

    /** Consent records and deletion requests: Legal & Governance Officer or System Administrator. */
    public void requireStaff() {
        if (!isStaff()) {
            throw new AccessDeniedException("Only the Legal & Governance Officer or the System Administrator can open this page.");
        }
    }

    public Optional<User> byId(Long id) {
        return id == null ? Optional.empty() : users.findById(id);
    }

    /** Looks up many accounts at once (for the user column in tables). */
    public Map<Long, User> byIds(Collection<Long> ids) {
        Map<Long, User> map = new HashMap<>();
        Set<Long> unique = new HashSet<>(ids);
        unique.remove(null);
        if (!unique.isEmpty()) {
            for (User user : users.findAllById(unique)) {
                map.put(user.getId(), user);
            }
        }
        return map;
    }

    /** Finds an account by id number, e-mail address or username. */
    public Optional<User> find(String query) {
        String q = PolicyHtml.trim(query);
        if (q.isEmpty()) {
            return Optional.empty();
        }
        if (q.matches("\\d{1,18}")) {
            Optional<User> byId = users.findById(Long.valueOf(q));
            if (byId.isPresent()) {
                return byId;
            }
        }
        Optional<User> byEmail = users.findByEmail(q);
        return byEmail.isPresent() ? byEmail : users.findByUsername(q);
    }

    public static String roleLabel(User user) {
        if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
            return "User";
        }
        List<String> labels = new ArrayList<>();
        for (Role role : user.getRoles()) {
            labels.add(roleLabel(role.getName().name()));
        }
        Collections.sort(labels);
        return String.join(", ", labels);
    }

    public static String roleLabel(String authority) {
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

    public static boolean isAdministrator(User user) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        for (Role role : user.getRoles()) {
            if (role.getName() == RoleName.ROLE_SYSTEM_ADMINISTRATOR) {
                return true;
            }
        }
        return false;
    }

    public static String clientIp(HttpServletRequest request) {
        return PolicyHtml.limit(request.getRemoteAddr(), 45);
    }

    public static String userAgent(HttpServletRequest request) {
        return PolicyHtml.limit(request.getHeader("User-Agent"), 255);
    }

    private static Authentication authentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return null;
        }
        return auth;
    }
}
