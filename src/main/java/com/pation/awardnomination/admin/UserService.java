package com.pation.awardnomination.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        String strRole = signUpRequest.getRole();

        boolean isVoter = false;
        if (strRole == null) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_VOTER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            isVoter = true;
        } else {
            RoleName roleName = RoleName.valueOf(strRole);
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(role);
            if (roleName == RoleName.ROLE_VOTER) {
                isVoter = true;
            }
        }

        user.setRoles(roles);
        
        // If registering as a voter, require email verification
        if (isVoter) {
            user.setEmailVerified(false);
        } else {
            user.setEmailVerified(true);
        }

        return userRepository.save(user);
    }

    public List<UserInfoResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToUserInfo).collect(Collectors.toList());
    }

    public UserInfoResponse mapToUserInfo(User user) {
        List<String> roles = user.getRoles().stream()
                .map(item -> item.getName().name())
                .collect(Collectors.toList());
        return new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), roles, user.isActive(), user.isHasVoted());
    }

    private void checkNotCurrentUser(User user) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName().equals(user.getEmail())) {
            throw new RuntimeException("Error: You cannot perform this action on your own account!");
        }
    }

    public void deactivateUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            checkNotCurrentUser(user);
            user.setActive(false);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Error: User not found");
        }
    }

    public void reactivateUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            checkNotCurrentUser(user);
            user.setActive(true);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Error: User not found");
        }
    }

    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            checkNotCurrentUser(user);
            userRepository.deleteById(userId);
        } else {
            throw new RuntimeException("Error: User not found");
        }
    }

    public void updateRole(Long userId, String newRoleStr) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        
        checkNotCurrentUser(user);
        
        RoleName roleName = RoleName.valueOf(newRoleStr);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        userRepository.save(user);
    }

    public void updateUserDetails(Long userId, String username, String email, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        
        checkNotCurrentUser(user);

        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        user.setUsername(username);
        user.setEmail(email);

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found"));
    }
}
