package com.pation.awardnomination.evaluation;

import org.springframework.stereotype.Component;

/** OOP - POLYMORPHISM: one ConflictChecker implementation (same organization = conflict). */
@Component
public class OrganizationConflictChecker implements ConflictChecker {

    @Override
    public boolean hasConflict(Judge judge, ShortlistEntry nominee) {
        return judge.getOrganization().trim().equalsIgnoreCase(nominee.getOrganization().trim());
    }

    @Override
    public String describe(Judge judge, ShortlistEntry nominee) {
        return "Conflict of interest: judge " + judge.getName() + " and nominee "
                + nominee.getNomineeName() + " are both from " + judge.getOrganization();
    }
}
