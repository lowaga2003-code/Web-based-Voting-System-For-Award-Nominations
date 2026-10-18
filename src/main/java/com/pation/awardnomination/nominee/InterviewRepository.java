package com.pation.awardnomination.nominee;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Integer> {
    // Nominee කෙනෙක්ට තමන් Book කරගත්තු Interview එක හොයාගන්න
    Interview findByNomineeNic(String nomineeNic);

    // සියලුම Available slots හොයාගන්න
    List<Interview> findByStatus(String status);
}