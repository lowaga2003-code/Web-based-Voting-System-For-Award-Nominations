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
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/dashboard"; // default

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_SYSTEM_ADMINISTRATOR")) {
                redirectUrl = "/admin/users";
                break;
            } else if (role.equals("ROLE_VOTER")) {
                // Trigger 2FA
                com.pation.awardnomination.admin.User user = userRepository.findByEmail(authentication.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                otpService.generateAndSendOtp(user, "Login");
                
                request.getSession().setAttribute("pendingUserId", user.getId());
                
                // Clear the security context so they are not fully authenticated yet
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
                
                redirectUrl = "/verify-login";
                break;
            } else if (role.equals("ROLE_NOMINEE")) {
                redirectUrl = "/nominee/form";
                break;
            } else if (role.equals("ROLE_PROGRAM_MANAGER")) {
                redirectUrl = "/manager/categories";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
