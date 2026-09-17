package com.pation.awardnomination.nominee;

import java.util.ArrayList;
import java.util.List;

public class NomineeManagerImpl implements NomineeManager {

    // DATA STRUCTURE: Using a simple Java Collection to store nominees.
    private List<Nominee> nomineeList;

    // Constructor
    public NomineeManagerImpl() {
        this.nomineeList = new ArrayList<>();
    }

    // POLYMORPHISM: We are overriding the abstract method from the interface to provide specific behavior.
    @Override
    public void addNominee(Nominee nominee) throws NomineeException {
        // EXCEPTION HANDLING: Using try-catch blocks to handle potential errors safely.
        try {
            if (nominee.getFullName() == null || nominee.getFullName().isEmpty()) {
                // Throwing our custom exception
                throw new NomineeException("Nominee name cannot be empty!");
            }

            nomineeList.add(nominee);
            System.out.println("Nominee added successfully: " + nominee.getFullName());

        } catch (Exception e) {
            throw new NomineeException("Error while adding nominee: " + e.getMessage());
        }
    }

    // POLYMORPHISM: Overriding the second method.
    @Override
    public List<Nominee> getAllNominees() {
        return nomineeList;
    }
}