package com.pation.awardnomination.dispute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for DisputeTicket entity.
 */
@Repository
public interface DisputeTicketRepository extends JpaRepository<DisputeTicket, Long> {
    List<DisputeTicket> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<DisputeTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);
    List<DisputeTicket> findAllByOrderByCreatedAtDesc();
    Optional<DisputeTicket> findByTicketCode(String ticketCode);
    long countByStatus(TicketStatus status);
}
