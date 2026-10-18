package com.pation.awardnomination.nominee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NomineeRepository extends JpaRepository<Nominee, Integer> {

    // NIC එකෙන් Nominee කෙනෙක්ව හොයාගන්න මේ පේළිය අනිවාර්යයි!

    Nominee findFirstByNic(String nic);

}