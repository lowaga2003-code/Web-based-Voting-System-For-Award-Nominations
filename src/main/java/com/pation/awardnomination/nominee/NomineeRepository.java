
package com.pation.awardnomination.nominee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// ABSTRACTION & INHERITANCE: Inheriting built-in database methods from JpaRepository.
public interface NomineeRepository extends JpaRepository<Nominee, Integer> {
}