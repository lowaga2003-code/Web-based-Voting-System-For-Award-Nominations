package com.pation.awardnomination.voting;

import com.pation.awardnomination.category.AwardCategory;
import com.pation.awardnomination.category.CategoryEntry;
import com.pation.awardnomination.category.CategoryPhase;
import com.pation.awardnomination.category.EligibilityRule;
import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.nominee.Nominee;

import java.util.List;

/** Voting - read-only view models handed to the JSP pages. */
public final class BallotViews {

    private BallotViews() { }

    /** One category on the voter's ballot home page. */
    public static class Card {
        private final AwardCategory category;
        private final CategoryPhase phase;
        private final long nominees;
        private final Vote myVote;

        public Card(AwardCategory category, CategoryPhase phase, long nominees, Vote myVote) {
            this.category = category;
            this.phase = phase;
            this.nominees = nominees;
            this.myVote = myVote;
        }

        public AwardCategory getCategory() { return category; }
        public CategoryPhase getPhase() { return phase; }
        public long getNominees() { return nominees; }
        public Vote getMyVote() { return myVote; }
        public boolean isVoted() { return myVote != null; }
        public boolean isCanVote() { return myVote == null && phase == CategoryPhase.VOTING_OPEN && nominees > 0; }
    }

    /** One nominee on a category ballot. */
    public static class Option {
        private final CategoryEntry entry;
        private final Nominee nominee;

        public Option(CategoryEntry entry, Nominee nominee) {
            this.entry = entry;
            this.nominee = nominee;
        }

        public CategoryEntry getEntry() { return entry; }
        public Nominee getNominee() { return nominee; }
        public Integer getNomineeId() { return entry.getNomineeId(); }
        public String getName() { return nominee.getFullName() == null ? entry.getNomineeName() : nominee.getFullName(); }
        public String getOrganisation() { return nominee.getCompanyName(); }
        public String getBiography() { return nominee.getBiography(); }
        public boolean isEvidenceOnFile() { return nominee.getEvidenceFileName() != null && !nominee.getEvidenceFileName().isBlank(); }
    }

    /** Everything the category ballot page needs. */
    public static class Page {
        private final AwardCategory category;
        private final CategoryPhase phase;
        private final List<Option> options;
        private final List<EligibilityRule> rules;
        private final Vote myVote;
        private final String myChoice;

        public Page(AwardCategory category, CategoryPhase phase, List<Option> options, List<EligibilityRule> rules,
                    Vote myVote, String myChoice) {
            this.category = category;
            this.phase = phase;
            this.options = options;
            this.rules = rules;
            this.myVote = myVote;
            this.myChoice = myChoice;
        }

        public AwardCategory getCategory() { return category; }
        public CategoryPhase getPhase() { return phase; }
        public List<Option> getOptions() { return options; }
        public List<EligibilityRule> getRules() { return rules; }
        public Vote getMyVote() { return myVote; }
        public String getMyChoice() { return myChoice; }
        public boolean isOpen() { return phase == CategoryPhase.VOTING_OPEN && myVote == null && !options.isEmpty(); }
    }

    /** A voter's own receipt. */
    public static class Receipt {
        private final Vote vote;
        private final AwardCategory category;
        private final String nomineeName;
        private final String organisation;
        private final boolean sealIntact;
        private final AuditTransparencyLog ledgerEntry;

        public Receipt(Vote vote, AwardCategory category, String nomineeName, String organisation,
                       boolean sealIntact, AuditTransparencyLog ledgerEntry) {
            this.vote = vote;
            this.category = category;
            this.nomineeName = nomineeName;
            this.organisation = organisation;
            this.sealIntact = sealIntact;
            this.ledgerEntry = ledgerEntry;
        }

        public Vote getVote() { return vote; }
        public AwardCategory getCategory() { return category; }
        public String getNomineeName() { return nomineeName; }
        public String getOrganisation() { return organisation; }
        public boolean isSealIntact() { return sealIntact; }
        public AuditTransparencyLog getLedgerEntry() { return ledgerEntry; }
    }

    /** One nominee's line in the public results. */
    public static class Row {
        private final String name;
        private final String organisation;
        private final long votes;
        private final int percent;
        private final boolean leader;

        public Row(String name, String organisation, long votes, int percent, boolean leader) {
            this.name = name;
            this.organisation = organisation;
            this.votes = votes;
            this.percent = percent;
            this.leader = leader;
        }

        public String getName() { return name; }
        public String getOrganisation() { return organisation; }
        public long getVotes() { return votes; }
        public int getPercent() { return percent; }
        public boolean isLeader() { return leader; }
    }

    /** Public result of one category (numbers only once voting has closed). */
    public static class Result {
        private final AwardCategory category;
        private final CategoryPhase phase;
        private final long total;
        private final List<Row> rows;

        public Result(AwardCategory category, CategoryPhase phase, long total, List<Row> rows) {
            this.category = category;
            this.phase = phase;
            this.total = total;
            this.rows = rows;
        }

        public AwardCategory getCategory() { return category; }
        public CategoryPhase getPhase() { return phase; }
        public long getTotal() { return total; }
        public List<Row> getRows() { return rows; }
        public boolean isPublished() { return phase == CategoryPhase.RESULTS; }
    }
}
