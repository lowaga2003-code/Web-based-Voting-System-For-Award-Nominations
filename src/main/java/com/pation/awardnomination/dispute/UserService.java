package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service managing user authentication and retrieval.
 */
@Service("disputeUserService") // [INTEGRATION] bean name "userService" is already used by Module 1 (admin)
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> authenticate(String identifier, String password, UserRole role) {
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(identifier);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password) && (role == null || user.getRole() == role)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
