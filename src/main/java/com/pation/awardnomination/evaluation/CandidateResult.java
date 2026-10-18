package com.pation.awardnomination.evaluation;

/** [INTEGRATION - new] A candidate's place in the results of one category. */
public class CandidateResult {

    private final ShortlistEntry entry;
    private final Double average;       // average of the judges' complete totals, null = not scored yet
    private final int completeSheets;
    private final int panelSize;
    private int rank;
    private Long publicVotes;           // [INTEGRATION v5] null = no published public vote for this category
    private int votePercent;

    public CandidateResult(ShortlistEntry entry, Double average, int completeSheets, int panelSize) {
        this.entry = entry;
        this.average = average;
        this.completeSheets = completeSheets;
        this.panelSize = panelSize;
    }

    void setRank(int rank) { this.rank = rank; }

    void setPublicVotes(long votes, long totalInCategory) {
        this.publicVotes = votes;
        this.votePercent = totalInCategory <= 0 ? 0 : (int) Math.round(votes * 100.0 / totalInCategory);
    }

    public ShortlistEntry getEntry() { return entry; }
    public Double getAverage() { return average; }
    public int getCompleteSheets() { return completeSheets; }
    public int getPanelSize() { return panelSize; }
    public int getRank() { return rank; }
    public Long getPublicVotes() { return publicVotes; }
    public int getVotePercent() { return votePercent; }
}
