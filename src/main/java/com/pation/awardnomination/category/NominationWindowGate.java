package com.pation.awardnomination.category;

import com.pation.awardnomination.nominee.NomineeController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Module 2 -> Module 3 deadline lock (PDF: "when the Module 2 deadline ends, Module 3's
 * Update and Delete are locked automatically").
 *
 * This interceptor runs in front of the nominee controller, so the nominee code itself is not
 * changed. It only looks at which NomineeController method is about to run:
 *   - showForm / submitNominee     : a new nomination needs an open nomination window
 *   - showEditForm / updateNominee : editing is locked once that nominee's deadline has passed
 * The System Administrator is never blocked, because the review and approval happen after the deadline.
 */
@Component
public class NominationWindowGate implements HandlerInterceptor {

    private static final Set<String> NEW_NOMINATION = Set.of("showForm", "submitNominee");
    private static final Set<String> CHANGE_PROFILE = Set.of("showEditForm", "updateNominee");

    private final CategoryService categoryService;

    public NominationWindowGate(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod method = (HandlerMethod) handler;
        if (!NomineeController.class.isAssignableFrom(method.getBeanType())) return true;
        String name = method.getMethod().getName();
        boolean isNew = NEW_NOMINATION.contains(name);
        boolean isChange = CHANGE_PROFILE.contains(name);
        if (!isNew && !isChange) return true;
        if (isAdministrator()) return true;

        LocalDateTime now = categoryService.now();
        NominationLocks locks = categoryService.locks();
        if (isNew) {
            if (locks.acceptingNew()) return true;
            response.sendRedirect(request.getContextPath() + "/timeline/locked?reason=closed");
            return false;
        }
        Integer nomineeId = lastPathNumber(request.getRequestURI());
        if (nomineeId == null || !locks.isLocked(nomineeId, now)) return true;
        response.sendRedirect(request.getContextPath() + "/timeline/locked?reason=locked&nominee=" + nomineeId);
        return false;
    }

    private static boolean isAdministrator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_SYSTEM_ADMINISTRATOR".equals(a.getAuthority())) return true;
        }
        return false;
    }

    /** "/nominee/update/12" gives 12; anything else gives null. */
    static Integer lastPathNumber(String uri) {
        if (uri == null) return null;
        String path = uri;
        int q = path.indexOf('?');
        if (q >= 0) path = path.substring(0, q);
        while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        int slash = path.lastIndexOf('/');
        String last = slash >= 0 ? path.substring(slash + 1) : path;
        if (!last.matches("\\d{1,9}")) return null;
        return Integer.valueOf(last);
    }
}
