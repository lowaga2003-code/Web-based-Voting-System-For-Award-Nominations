package com.pation.awardnomination.admin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for H2 console & simplicity in this phase
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Allow H2 console access
                // [INTEGRATION] Module 4's own old login API lives under /api/auth too - keep it away from the public
                // (/api/auth/users returned every voter with their password). Must stay above "/api/auth/**".
                .requestMatchers("/api/auth/login", "/api/auth/users").hasAnyRole("COMPLIANCE_AUDITOR", "SYSTEM_ADMINISTRATOR")
                .requestMatchers("/api/auth/**", "/login", "/register", "/verify-login", "/verify-registration").permitAll()   // Allow public endpoints
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll() // [INTEGRATION] shared styles must load on the login page too
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("SYSTEM_ADMINISTRATOR")
                // [INTEGRATION v5] Module 2 - Award Category Management: Programme Manager (+ System Administrator)
                .requestMatchers("/api/manager/**", "/manager/**").hasAnyRole("PROGRAM_MANAGER", "SYSTEM_ADMINISTRATOR")
                .requestMatchers("/api/voter/**", "/voter/**").hasRole("VOTER")
                // [INTEGRATION] Nominee module pages that only an administrator may open (must stay above "/nominee/**")
                .requestMatchers("/nominee/list", "/nominee/delete/**", "/nominee/interview/add").hasRole("SYSTEM_ADMINISTRATOR")
                .requestMatchers("/nominee/**").hasAnyRole("NOMINEE", "SYSTEM_ADMINISTRATOR")
                // [INTEGRATION] Module 4 - Audit & Dispute: auditor hub + REST APIs for auditors/admin,
                // dispute pages + transparency ledger for every logged-in user
                .requestMatchers("/auditor/**", "/api/auditor/**", "/api/disputes/**").hasAnyRole("COMPLIANCE_AUDITOR", "SYSTEM_ADMINISTRATOR")
                .requestMatchers("/dispute/**", "/api/audit-logs/**").authenticated()
                // [INTEGRATION] Module 6 - Policy & Consent: the documents are public (linked from the sign-up form) ...
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/policy/privacy", "/policy/terms", "/policy/view/**", "/api/policy/current").permitAll()
                // ... only the Legal & Governance Officer may change policies (LegalOfficerController also uses @PreAuthorize) ...
                .requestMatchers("/legal/policies/*/edit").hasRole("LEGAL_GOVERNANCE_OFFICER")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/legal/policies/**").hasRole("LEGAL_GOVERNANCE_OFFICER")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/policy/**").hasRole("LEGAL_GOVERNANCE_OFFICER")
                // ... the Legal Officer or the System Administrator review consents and deletion requests ...
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/deletion-request/**").hasAnyRole("LEGAL_GOVERNANCE_OFFICER", "SYSTEM_ADMINISTRATOR")
                .requestMatchers("/legal/**").hasAnyRole("LEGAL_GOVERNANCE_OFFICER", "SYSTEM_ADMINISTRATOR")
                // ... and every signed-in user has their own consent page and requests
                .requestMatchers("/privacy/**", "/api/consent/**", "/api/deletion-request/**").authenticated()
                // [INTEGRATION] Module 5 - Evaluation & Selection: Selection Committee Chairperson (+ System Administrator)
                .requestMatchers("/evaluation/**").hasAnyRole("SELECTION_COMMITTEE_CHAIRPERSON", "SYSTEM_ADMINISTRATOR")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied")) // [INTEGRATION] friendly 403 page instead of the white error page
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) // Required for H2 console
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email") // Use email for login
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        
        return http.build();
    }
}
