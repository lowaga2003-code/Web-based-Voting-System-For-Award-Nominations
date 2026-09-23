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
                .requestMatchers("/api/auth/**", "/login", "/register", "/verify-login", "/verify-registration").permitAll()   // Allow public endpoints
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("SYSTEM_ADMINISTRATOR")
                .requestMatchers("/api/manager/**", "/manager/**").hasRole("PROGRAM_MANAGER")
                .requestMatchers("/api/voter/**", "/voter/**").hasRole("VOTER")
                .requestMatchers("/nominee/**").hasAnyRole("NOMINEE", "SYSTEM_ADMINISTRATOR")
                .anyRequest().authenticated()
            )
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
