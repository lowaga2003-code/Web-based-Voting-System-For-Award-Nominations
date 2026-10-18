package com.pation.awardnomination.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for SystemActivityLog entity.
 */
@Repository
public interface SystemActivityLogRepository extends JpaRepository<SystemActivityLog, Long> {
    List<SystemActivityLog> findByVoterUsernameOrderByTimestampDesc(String voterUsername);
    List<SystemActivityLog> findTop20ByOrderByTimestampDesc();
}
