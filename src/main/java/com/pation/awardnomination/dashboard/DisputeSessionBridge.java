package com.pation.awardnomination.dashboard;

import com.pation.awardnomination.dispute.DisputeTicket;
import com.pation.awardnomination.dispute.DisputeTicketRepository;
import com.pation.awardnomination.dispute.DisputeViewController;
import com.pation.awardnomination.dispute.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * [INTEGRATION] Single sign-on bridge: main login (Module 1) -> Module 4 (Audit & Dispute).
 *
 * (සිංහලෙන්: Module 4 එකට තමන්ගේම users table එකක් (dispute_users) තියෙනවා. ඒ pages කවුද කියලා
 *  දැනගන්නේ ?userId= / auditorId= parameters වලින්. මේ class එක /dispute/** සහ /auditor/** request
 *  එකකට කලින් run වෙලා:
 *   1. main login account එකට ගැළපෙන Module 4 user කෙනෙක් (email එකෙන්) හොයනවා, නැත්නම් හදනවා;
 *   2. ඒ id එක session එකේ "disputeUserId" විදියට දානවා (auditor forms වලට);
 *   3. වෙන කෙනෙක්ගේ userId / auditorId එකක් දාලා වැඩ කරන්න දෙන්නේ නැහැ.
 *  Module 4 member ගේ logic එක වෙනස් කරලා නැහැ.)
 *
 * Registered for /dispute/** and /auditor/** in {@link PortalWebConfig}.
 */
@Component
public class DisputeSessionBridge implements HandlerInterceptor {

    /** Session attribute read by the Module 4 JSPs (auditor forms) - the logged-in user's Module 4 id. */
    public static final String DISPUTE_USER_ATTR = "disputeUserId";

    private final com.pation.awardnomination.dispute.UserRepository disputeUsers;
    private final com.pation.awardnomination.admin.UserRepository mainUsers;
    private final DisputeTicketRepository tickets;

    public DisputeSessionBridge(com.pation.awardnomination.dispute.UserRepository disputeUsers,
                                com.pation.awardnomination.admin.UserRepository mainUsers,
                                DisputeTicketRepository tickets) {
        this.disputeUsers = disputeUsers;
        this.mainUsers = mainUsers;
        this.tickets = tickets;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return true; // guests never get here - Spring Security sends them to /login
        }

        Set<String> roles = new HashSet<>();
        for (GrantedAuthority authority : auth.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
        boolean staff = roles.contains("ROLE_COMPLIANCE_AUDITOR") || roles.contains("ROLE_SYSTEM_ADMINISTRATOR");

        Long myId = linkedUser(auth.getName(), roles).getId();
        request.getSession().setAttribute(DISPUTE_USER_ATTR, myId);
        String me = String.valueOf(myId);

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.getBeanType() != DisputeViewController.class) {
            return true;
        }

        switch (handlerMethod.getMethod().getName()) {
            // "My disputes" pages always open for the logged-in user (their links have no ?userId=, default was 1)
            case "viewDisputeList":
            case "showDisputeForm":
                if (!me.equals(request.getParameter("userId"))) {
                    response.sendRedirect(request.getRequestURI() + "?userId=" + myId);
                    return false;
                }
                return true;

            // Voter actions: only as yourself
            case "submitDispute":
            case "deleteDisputeByUser":
            case "submitAdditionalEvidence":
            case "acknowledgeResolution":
                return allowOnly(me.equals(request.getParameter("userId")), request, response);

            // Auditor actions: only staff, and only as yourself (was a hard-coded auditorId=3)
            case "escalateToInvestigation":
            case "resolveAndArchive":
            case "deleteDisputeByAuditor":
                return allowOnly(staff && me.equals(request.getParameter("auditorId")), request, response);

            // Ticket details: auditors/admin see every ticket; everyone else only their own, in the voter view
            case "viewDisputeDetail":
                if (staff) {
                    return true;
                }
                if ("AUDITOR".equalsIgnoreCase(request.getParameter("role"))) {
                    return allowOnly(false, request, response);
                }
                return allowOnly(ownsTicket(request, myId), request, response);

            default:
                return true; // e.g. the transparency ledger is open to every logged-in user
        }
    }

    /** Finds the Module 4 user that belongs to this main-login account (same email), or creates it. */
    private com.pation.awardnomination.dispute.User linkedUser(String email, Set<String> roles) {
        UserRole role = roles.contains("ROLE_SYSTEM_ADMINISTRATOR") ? UserRole.SYSTEM_ADMIN
                : roles.contains("ROLE_COMPLIANCE_AUDITOR") ? UserRole.COMPLIANCE_AUDITOR
                : UserRole.VOTER; // every other account is a "Registered User" in Module 4

        Optional<com.pation.awardnomination.dispute.User> existing = disputeUsers.findByEmail(email);
        if (existing.isPresent()) {
            com.pation.awardnomination.dispute.User user = existing.get();
            if (user.getRole() != role) { // role was changed in Module 1 -> keep Module 4 in step
                user.setRole(role);
                user = disputeUsers.save(user);
            }
            return user;
        }

        String name = mainUsers.findByEmail(email)
                .map(com.pation.awardnomination.admin.User::getUsername)
                .orElse(email);
        com.pation.awardnomination.dispute.User created = new com.pation.awardnomination.dispute.User(
                uniqueUsername(name),
                email,
                UUID.randomUUID().toString(), // never used: people sign in through the main login
                limit(name, 100),
                role);
        try {
            return disputeUsers.save(created);
        } catch (RuntimeException e) {
            // two first requests at the same moment -> the other one already created it
            return disputeUsers.findByEmail(email).orElseThrow(() -> e);
        }
    }

    private String uniqueUsername(String name) {
        String base = limit(name, 40);
        String candidate = base;
        int n = 1;
        while (disputeUsers.findByUsername(candidate).isPresent()) {
            n++;
            candidate = base + "_" + n;
        }
        return candidate;
    }

    private static String limit(String value, int max) {
        return value.length() > max ? value.substring(0, max) : value;
    }

    private boolean ownsTicket(HttpServletRequest request, Long myId) {
        Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(vars instanceof Map)) {
            return false;
        }
        Object id = ((Map<?, ?>) vars).get("id");
        if (id == null || !id.toString().matches("\\d{1,18}")) {
            return false;
        }
        DisputeTicket ticket = tickets.findById(Long.valueOf(id.toString())).orElse(null);
        return ticket != null && ticket.getUser() != null && myId.equals(ticket.getUser().getId());
    }

    private static boolean allowOnly(boolean allowed, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!allowed) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
        }
        return allowed;
    }
}
