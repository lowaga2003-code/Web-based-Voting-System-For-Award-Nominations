package com.pation.awardnomination.nominee;

import java.util.List;

// ABSTRACTION: We only define WHAT actions can be done, hiding HOW they are done.
public interface NomineeManager {

    // Method to add a new nominee
    void addNominee(Nominee nominee) throws NomineeException;

    // Method to view all nominees
    List<Nominee> getAllNominees();
}
