package com.pation.awardnomination.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/verify-login")
    public String verifyLoginPage() {
        return "verify-login";
    }

    @GetMapping("/verify-registration")
    public String verifyRegistrationPage() {
        return "verify-registration";
    }

    @GetMapping("/admin/users")
    public String adminUsersPage(org.springframework.ui.Model model, java.security.Principal principal) {
        if (principal != null) {
            model.addAttribute("currentUserEmail", principal.getName());
        }
        return "admin-users";
    }

    // [INTEGRATION v5] The old placeholder URLs now open the real pages
    // (Voting: com.pation.awardnomination.voting, Module 2: com.pation.awardnomination.category).
    @GetMapping("/voter/vote")
    public String voterDashboard() {
        return "redirect:/voter/ballot"; // was: "dashboard" (generic placeholder)
    }

    // [INTEGRATION] REMOVED @GetMapping("/nominee/profile") placeholder.
    // NomineeController already owns /nominee/profile - two handlers for one URL stop Spring Boot from starting.

    @GetMapping("/manager/categories")
    public String managerDashboard() {
        return "redirect:/manager/awards"; // was: "dashboard" (generic placeholder)
    }

    // [INTEGRATION] REMOVED @GetMapping("/dashboard") placeholder.
    // The real dashboard is com.pation.awardnomination.dashboard.DashboardController.
}
