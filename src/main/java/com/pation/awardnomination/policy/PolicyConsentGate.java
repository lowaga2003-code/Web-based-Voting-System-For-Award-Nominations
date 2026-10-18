package com.pation.awardnomination.policy;

import com.pation.awardnomination.admin.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

/**
 * MODULE 6 - "consent is required before the user can continue".
 *
 * (සිංහලෙන්: Log වුණ user කෙනෙක් current Privacy Policy / Terms එකට agree වෙලා නැත්නම්
 *  (අලුත් account එකක්, නැත්නම් Legal Officer major update එකක් publish කරලා), dashboard එකට හෝ
 *  වෙන module page එකකට යන්න කලින් /privacy/consent page එකට යවනවා. Agree වුණාම ආපහු ගිය page එකට.
 *  Voting pages (/voter/**) වලට යන්න කලින් voting consent checkbox එක තියෙන /privacy/voting page එකටත් යවනවා.)
 *
 * Registered in PortalWebConfig for the page areas of all modules. /privacy/** and /policy/** are not
 * included, so a user who does not agree can still read the documents and ask for their data to be deleted.
 */
@Component
public class PolicyConsentGate implements HandlerInterceptor {

    static final String VOTING_AREA = "/voter/";

    private final ConsentService consentService;
    private final PolicyAccounts accounts;

    public PolicyConsentGate(ConsentService consentService, PolicyAccounts accounts) {
        this.consentService = consentService;
        this.accounts = accounts;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Optional<User> account = accounts.current();
        if (account.isEmpty()) {
            return true; // guests are handled by Spring Security
        }
        Long userId = account.get().getId();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String target;
        if (!consentService.mustAccept(userId).isEmpty()) {
            target = "/privacy/consent";                        // new account, withdrawn consent or a major update
        } else if (path.startsWith(VOTING_AREA) && !consentService.missingVotingConsent(userId).isEmpty()) {
            target = "/privacy/voting";                         // the voting page's own consent checkbox
        } else {
            return true;                                        // everything accepted - carry on
        }
        String next = "/dashboard";
        if ("GET".equalsIgnoreCase(request.getMethod())) {     // come back to the same page afterwards
            String query = request.getQueryString();
            next = query == null ? path : path + "?" + query;
        }
        response.sendRedirect(request.getContextPath() + target + "?next=" + PolicyHtml.urlEncode(next));
        return false;
    }
}
