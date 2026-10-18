package com.pation.awardnomination.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User entity.
 */
@Repository("disputeUserRepository") // [INTEGRATION] bean name "userRepository" is already used by Module 1 (admin)
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
}
