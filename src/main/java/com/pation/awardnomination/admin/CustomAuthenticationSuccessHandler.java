package com.pation.awardnomination.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @org.springframework.beans.factory.annotation.Autowired
    private com.pation.awardnomination.admin.UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.pation.awardnomination.admin.OtpService otpService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // [INTEGRATION] Fresh start for the Nominee module on every main login, so a new user in the same
        // browser never inherits the previous user's nominee NIC session.
        com.pation.awardnomination.dashboard.NomineeSessionBridge.resetNomineeSession(request.getSession());

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/dashboard"; // default

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_SYSTEM_ADMINISTRATOR")) {
                redirectUrl = "/dashboard"; // [INTEGRATION] was "/admin/users" - admins land on the main dashboard
                break;
            } else if (role.equals("ROLE_VOTER")) {
                // Trigger 2FA
                com.pation.awardnomination.admin.User user = userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                otpService.generateAndSendOtp(user, "Login");
                
                request.getSession().setAttribute("pendingUserId", user.getId());
                
                // Clear the security context so they are not fully authenticated yet
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
                // [INTEGRATION FIX] Spring Security saves the login into the session BEFORE this handler runs,
                // so clearContext() alone left the voter logged in and the OTP page could be skipped.
                request.getSession().removeAttribute(
                        org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

                redirectUrl = "/verify-login";
                break;
            } else if (role.equals("ROLE_NOMINEE")) {
                redirectUrl = "/dashboard"; // [INTEGRATION] was "/nominee/form"
                break;
            } else if (role.equals("ROLE_PROGRAM_MANAGER")) {
                redirectUrl = "/dashboard"; // [INTEGRATION] was "/manager/categories"
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
