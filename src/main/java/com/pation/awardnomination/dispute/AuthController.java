package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller handling user authentication and role switching.
 */
@RestController("disputeAuthController") // [INTEGRATION] bean name "authController" is already used by Module 1 (admin)
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public static class LoginRequest {
        private String usernameOrEmail;
        private String password;
        private UserRole role;

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public UserRole getRole() { return role; }
        public void setRole(UserRole role) { this.role = role; }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userService.authenticate(request.getUsernameOrEmail(), request.getPassword(), request.getRole());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("id", user.getId());
            resp.put("username", user.getUsername());
            resp.put("fullName", user.getFullName());
            resp.put("email", user.getEmail());
            resp.put("role", user.getRole());
            resp.put("roleDisplayName", user.getRole().getDisplayName());
            return ResponseEntity.ok(resp);
        }

        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", "Invalid credentials or unauthorized role. Please check username and password.");
        return ResponseEntity.status(401).body(err);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getUsersByRole(UserRole.VOTER));
    }
}
