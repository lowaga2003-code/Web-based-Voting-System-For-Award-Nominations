package com.pation.awardnomination.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Roles
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        }

        // Seed Users
        createUserIfNotFound("admin", "admin@test.com", "password", RoleName.ROLE_SYSTEM_ADMINISTRATOR);
        createUserIfNotFound("manager", "manager@test.com", "password", RoleName.ROLE_PROGRAM_MANAGER);
        createUserIfNotFound("voter1", "voter1@test.com", "password", RoleName.ROLE_VOTER);
        createUserIfNotFound("nominee1", "nominee1@test.com", "password", RoleName.ROLE_NOMINEE);
        createUserIfNotFound("auditor", "auditor@test.com", "password", RoleName.ROLE_COMPLIANCE_AUDITOR);
        createUserIfNotFound("chairperson", "chair@test.com", "password", RoleName.ROLE_SELECTION_COMMITTEE_CHAIRPERSON);
        createUserIfNotFound("legal", "legal@test.com", "password", RoleName.ROLE_LEGAL_GOVERNANCE_OFFICER);
    }

    private void createUserIfNotFound(String username, String email, String password, RoleName roleName) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(username, email, passwordEncoder.encode(password));
            Set<Role> roles = new HashSet<>();
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
}
