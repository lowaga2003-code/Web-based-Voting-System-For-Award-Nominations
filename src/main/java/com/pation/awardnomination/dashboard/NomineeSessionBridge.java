package com.pation.awardnomination.dashboard;

import com.pation.awardnomination.nominee.Nominee;
import com.pation.awardnomination.nominee.NomineeController;
import com.pation.awardnomination.nominee.NomineeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * [INTEGRATION] Single sign-on bridge: main login (admin module) -> Nominee module.
 *
 * (සිංහලෙන්: Nominee module එක "කවුද log වෙලා ඉන්නේ" කියලා බලන්නේ session එකේ "role" attribute එකෙන්.
 *  "ADMIN" නම් admin features, "USER" නම් nominee (NIC login). මේ class එක /nominee/** request
 *  එකක් NomineeController එකට යන්න කලින් run වෙලා, main login එකේ admin කෙනෙක් නම් session එකට
 *  "role" = "ADMIN" දානවා. ඒ නිසා admin ට nominee part එකට ආයෙත් log වෙන්න ඕනේ නැහැ.
 *  NomineeController එකේ එක line එකක්වත් වෙනස් කරලා නැහැ.)
 *
 * It also closes two gaps for accounts that are NOT administrators:
 *  - the nominee module's old built-in admin login (loginType=admin) cannot be used;
 *  - a nominee can open / save only their OWN nomination (/nominee/edit/{id}, /nominee/update/{id}).
 *
 * Registered for /nominee/** in {@link PortalWebConfig}.
 */
@Component
public class NomineeSessionBridge implements HandlerInterceptor {

    /** Session attribute names used by NomineeController (must match exactly). */
    static final String NOMINEE_ROLE_ATTR = "role";
    static final String NOMINEE_NIC_ATTR = "userNic";
    static final String NOMINEE_ADMIN = "ADMIN";

    /** Main-login roles that get ADMIN rights inside the Nominee module. Add more here later if needed. */
    static final Set<String> NOMINEE_ADMIN_ROLES = Set.of("ROLE_SYSTEM_ADMINISTRATOR");

    private final NomineeRepository nomineeRepository;

    public NomineeSessionBridge(NomineeRepository nomineeRepository) {
        this.nomineeRepository = nomineeRepository;
    }

    /**
     * Forget any earlier nominee NIC login in this browser session.
     * Called on every successful main login, so a new user never inherits the previous user's nominee session.
     */
    public static void resetNomineeSession(HttpSession session) {
        session.removeAttribute(NOMINEE_ROLE_ATTR);
        session.removeAttribute(NOMINEE_NIC_ATTR);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return true; // guests never get here - Spring Security sends them to /login
        }

        HttpSession session = request.getSession();
        if (isNomineeAdmin(auth)) {
            // Admin on the main login -> admin inside the Nominee module as well (no second login)
            session.setAttribute(NOMINEE_ROLE_ATTR, NOMINEE_ADMIN);
            return true;
        }

        // ----- below here: the user is NOT an administrator on the main login -----
        if (NOMINEE_ADMIN.equals(session.getAttribute(NOMINEE_ROLE_ATTR))) {
            session.removeAttribute(NOMINEE_ROLE_ATTR); // never keep nominee admin rights
        }

        // The nominee module's old built-in admin login is closed for non-admin accounts
        if (isNomineeMethod(handler, "login") && "admin".equals(request.getParameter("loginType"))) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return false;
        }

        // A nominee may only open / save their OWN nomination (matched by the NIC they logged in with)
        if (isNomineeMethod(handler, "showEditForm") || isNomineeMethod(handler, "updateNominee")) {
            if (!ownsNomination(request, session)) {
                response.sendRedirect(request.getContextPath() + "/access-denied");
                return false;
            }
        }

        // Nominees (role "USER" + "userNic") still come from the nominee module's own NIC login - untouched.
        return true;
    }

    private boolean isNomineeAdmin(Authentication auth) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (NOMINEE_ADMIN_ROLES.contains(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /** true when the request is about to run the given method of NomineeController. */
    private boolean isNomineeMethod(Object handler, String methodName) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getBeanType() == NomineeController.class
                && handlerMethod.getMethod().getName().equals(methodName);
    }

    /** Checks that {id} in /nominee/edit/{id} or /nominee/update/{id} is the logged-in nominee's own record. */
    private boolean ownsNomination(HttpServletRequest request, HttpSession session) {
        Object nic = session.getAttribute(NOMINEE_NIC_ATTR);
        Object vars = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (nic == null || !(vars instanceof Map)) {
            return false;
        }
        Object id = ((Map<?, ?>) vars).get("id");
        if (id == null || !id.toString().matches("\\d{1,9}")) {
            return false;
        }
        Nominee nominee = nomineeRepository.findById(Integer.valueOf(id.toString())).orElse(null);
        return nominee != null && nic.equals(nominee.getNic());
    }
}
