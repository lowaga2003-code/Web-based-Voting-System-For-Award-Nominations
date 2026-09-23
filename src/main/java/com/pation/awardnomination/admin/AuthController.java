package com.pation.awardnomination.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.pation.awardnomination.admin.OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {
            com.pation.awardnomination.admin.User user = userService.registerUser(signUpRequest);
            
            if (!user.isEmailVerified()) {
                otpService.generateAndSendOtp(user, "Registration");
                // Return a specific response so the frontend knows it needs OTP verification
                return ResponseEntity.ok(java.util.Map.of("message", "User registered. OTP sent.", "requiresOtp", true, "userId", user.getId()));
            }
            
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<?> verifyRegistration(@RequestParam Long userId, @RequestParam String otpCode) {
        if (otpService.verifyOtp(userId, otpCode)) {
            userService.verifyEmail(userId);
            return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired OTP."));
    }

    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestParam String otpCode, jakarta.servlet.http.HttpSession session) {
        Long userId = (Long) session.getAttribute("pendingUserId");
        if (userId == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Session expired. Please log in again."));
        }

        if (otpService.verifyOtp(userId, otpCode)) {
            com.pation.awardnomination.admin.User user = userService.getUserById(userId);
            org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getName().name())).collect(java.util.stream.Collectors.toList()))
                .build();

            org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
            
            // Save context to session so it persists
            session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, org.springframework.security.core.context.SecurityContextHolder.getContext());
            session.removeAttribute("pendingUserId");

            return ResponseEntity.ok(new MessageResponse("Login successful!"));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired OTP."));
    }
    
    // Note: For actual authentication/login, typically Spring Security's AuthenticationManager is used to generate JWTs.
    // In this basic implementation, we rely on HTTP Basic auth, so /login might just return success if authenticated.
    @GetMapping("/login-check")
    public ResponseEntity<?> checkLogin() {
        return ResponseEntity.ok(new MessageResponse("Successfully authenticated."));
    }
}
