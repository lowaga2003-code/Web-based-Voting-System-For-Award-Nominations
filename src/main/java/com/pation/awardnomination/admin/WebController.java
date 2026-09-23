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

    @GetMapping("/voter/vote")
    public String voterDashboard() {
        return "dashboard"; // generic placeholder
    }

    @GetMapping("/nominee/profile")
    public String nomineeDashboard() {
        return "dashboard"; // generic placeholder
    }

    @GetMapping("/manager/categories")
    public String managerDashboard() {
        return "dashboard"; // generic placeholder
    }

    @GetMapping("/dashboard")
    public String genericDashboard() {
        return "dashboard"; // generic placeholder
    }
}
