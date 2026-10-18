package com.pation.awardnomination.voting;

import com.pation.awardnomination.admin.Role;
import com.pation.awardnomination.admin.RoleName;
import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.admin.UserRepository;
import com.pation.awardnomination.category.AwardCategory;
import com.pation.awardnomination.category.CategoryEntry;
import com.pation.awardnomination.category.CategoryException;
import com.pation.awardnomination.category.CategoryPhase;
import com.pation.awardnomination.category.CategoryService;
import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.nominee.Nominee;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Voting - the Registered User (Voter) casts one vote per award category.
 *
 * How it joins the other parts:
 *   Module 1 : who the voter is (users table, 2-step OTP login) and the hasVoted flag
 *   Module 2 : which categories exist, their voting window and their ballot (category_entries)
 *   Module 3 : the nominees' public profile shown on the ballot
 *   Module 4 : every sealed ballot and every duplicate attempt is logged
 *   Module 6 : the VOTING consent is asked by the policy gate before any /voter page opens
 */
@Service
public class VotingService {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm", Locale.ENGLISH);

    private final VoteRepository votes;
    private final CategoryService categories;
    private final UserRepository users;
    private final VotingAuditLogger audit;

    public VotingService(VoteRepository votes, CategoryService categories, UserRepository users, VotingAuditLogger audit) {
        this.votes = votes;
        this.categories = categories;
        this.users = users;
        this.audit = audit;
    }

    public LocalDateTime now() {
        return categories.now();
    }

    /** The signed-in voter (principal name is the e-mail address used to log in). */
    public User voter(String email) {
        User user = email == null ? null : users.findByEmail(email).orElse(null);
        if (user == null) throw new VotingException("Your account could not be found. Please sign in again.");
        if (!user.isActive()) throw new VotingException("This account is suspended, so it cannot vote.");
        if (!hasRole(user, RoleName.ROLE_VOTER)) throw new VotingException("Only registered voters can vote.");
        return user;
    }

    // ------------------------------------------------------------------ ballot pages

    /** Every active category with this voter's status, open ballots first. */
    public List<BallotViews.Card> cards(User voter) {
        LocalDateTime now = now();
        Map<Long, Vote> mine = new HashMap<>();
        for (Vote v : votes.findByVoterIdOrderByCastAtDesc(voter.getId())) mine.put(v.getCategoryId(), v);
        List<BallotViews.Card> cards = new ArrayList<>();
        for (AwardCategory c : categories.activeCategories()) {
            cards.add(new BallotViews.Card(c, c.phaseAt(now), categories.entryCount(c.getId()), mine.get(c.getId())));
        }
        cards.sort(Comparator.comparingInt(VotingService::order));
        return cards;
    }

    private static int order(BallotViews.Card card) {
        if (card.isCanVote()) return 0;
        if (card.getPhase() == CategoryPhase.VOTING_OPEN) return 1;
        if (card.getPhase() == CategoryPhase.BALLOT_PREP) return 2;
        if (card.getPhase() == CategoryPhase.NOMINATIONS_OPEN || card.getPhase() == CategoryPhase.SCHEDULED) return 3;
        return 4;
    }

    public BallotViews.Page page(User voter, Long categoryId) {
        AwardCategory c = activeCategory(categoryId);
        List<CategoryEntry> entries = categories.entries(c.getId());
        Map<Integer, Nominee> nominees = categories.nomineesById(ids(entries));
        List<BallotViews.Option> options = new ArrayList<>();
        for (CategoryEntry e : entries) {
            Nominee n = nominees.get(e.getNomineeId());
            if (n != null) options.add(new BallotViews.Option(e, n));
        }
        Vote mine = votes.findByVoterIdAndCategoryId(voter.getId(), c.getId()).orElse(null);
        String choice = mine == null ? null : nameOf(mine.getNomineeId(), entries, nominees);
        return new BallotViews.Page(c, c.phaseAt(now()), options, categories.rules(c.getId()), mine, choice);
    }

    // ------------------------------------------------------------------ cast a vote

    public Vote cast(User voter, Long categoryId, Integer nomineeId, boolean confirmed, String ip, String userAgent) {
        AwardCategory c = activeCategory(categoryId);
        LocalDateTime now = now();

        Optional<Vote> existing = votes.findByVoterIdAndCategoryId(voter.getId(), c.getId());
        if (existing.isPresent()) {
            audit.duplicateAttempt(voter, c, existing.get(), ip, userAgent);
            throw new DuplicateVoteException("You have already voted in " + c.getName()
                    + ". A second vote is not allowed and this attempt was logged for the Compliance Auditor.",
                    existing.get().getReceiptCode());
        }

        CategoryPhase phase = c.phaseAt(now);
        if (phase == CategoryPhase.RESULTS) {
            throw new VotingException("Voting for " + c.getName() + " closed on " + WHEN.format(c.getVotingEnd()) + ".");
        }
        if (phase != CategoryPhase.VOTING_OPEN) {
            throw new VotingException("Voting for " + c.getName() + " opens on " + WHEN.format(c.getVotingStart()) + ".");
        }
        if (nomineeId == null) throw new VotingException("Choose one nominee before casting your vote.");
        if (!confirmed) throw new VotingException("Tick the confirmation box: a vote cannot be changed after it is cast.");

        boolean onBallot = false;
        for (CategoryEntry e : categories.entries(c.getId())) {
            if (nomineeId.equals(e.getNomineeId())) onBallot = true;
        }
        if (!onBallot) throw new VotingException("That nominee is not on the " + c.getName() + " ballot.");
        if (categories.nomineesById(Collections.singletonList(nomineeId)).isEmpty()) {
            throw new VotingException("That nominee is no longer available.");
        }

        Vote vote = new Vote(voter.getId(), c.getId(), nomineeId, uniqueReceipt(), now);
        vote.seal(BallotSeal.newSalt());
        try {
            vote = votes.save(vote);
        } catch (DataIntegrityViolationException race) {
            // Two submissions at the same moment: the (voter_id, category_id) constraint kept only one.
            Vote first = votes.findByVoterIdAndCategoryId(voter.getId(), c.getId()).orElse(null);
            audit.duplicateAttempt(voter, c, first, ip, userAgent);
            throw new DuplicateVoteException("You have already voted in " + c.getName() + ".",
                    first == null ? null : first.getReceiptCode());
        }

        if (!voter.isHasVoted()) {          // Module 1: "has this account already voted?"
            voter.setHasVoted(true);
            users.save(voter);
        }
        audit.ballotSealed(vote, c, voter, ip, userAgent);
        return vote;
    }

    // ------------------------------------------------------------------ receipts and history

    public BallotViews.Receipt receipt(User voter, String code) {
        String key = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        Vote vote = votes.findByReceiptCode(key).orElse(null);
        if (vote == null || !vote.getVoterId().equals(voter.getId())) {
            throw new VotingException("That receipt was not found among your votes.");
        }
        AuditTransparencyLog ledgerRow = audit.ledgerEntry(vote.getReceiptCode()).orElse(null);
        return toReceipt(vote, ledgerRow);
    }

    public List<BallotViews.Receipt> history(User voter) {
        List<BallotViews.Receipt> list = new ArrayList<>();
        for (Vote v : votes.findByVoterIdOrderByCastAtDesc(voter.getId())) list.add(toReceipt(v, null));
        return list;
    }

    private BallotViews.Receipt toReceipt(Vote vote, AuditTransparencyLog ledgerRow) {
        AwardCategory c;
        try {
            c = categories.get(vote.getCategoryId());
        } catch (CategoryException e) {
            c = null;
        }
        List<CategoryEntry> entries = c == null ? Collections.emptyList() : categories.entries(c.getId());
        Map<Integer, Nominee> nominees = categories.nomineesById(Collections.singletonList(vote.getNomineeId()));
        Nominee n = nominees.get(vote.getNomineeId());
        String organisation = n != null ? n.getCompanyName() : null;
        if (organisation == null) {
            for (CategoryEntry e : entries) if (vote.getNomineeId().equals(e.getNomineeId())) organisation = e.getOrganisation();
        }
        return new BallotViews.Receipt(vote, c, nameOf(vote.getNomineeId(), entries, nominees), organisation,
                vote.isSealIntact(), ledgerRow);
    }

    // ------------------------------------------------------------------ public results

    /** Results per active category; numbers are published only after voting has closed. */
    public List<BallotViews.Result> results() {
        LocalDateTime now = now();
        List<BallotViews.Result> list = new ArrayList<>();
        for (AwardCategory c : categories.activeCategories()) {
            CategoryPhase phase = c.phaseAt(now);
            if (phase != CategoryPhase.RESULTS && phase != CategoryPhase.VOTING_OPEN) continue;
            if (phase != CategoryPhase.RESULTS) {
                list.add(new BallotViews.Result(c, phase, 0, Collections.emptyList()));
                continue;
            }
            list.add(new BallotViews.Result(c, phase, votes.countByCategoryId(c.getId()), rows(c)));
        }
        list.sort(Comparator.comparing((BallotViews.Result r) -> !r.isPublished())
                .thenComparing(r -> r.getCategory().getVotingEnd()));
        return list;
    }

    /** Vote count per nominee of a category, highest first (also used by Module 5). */
    public List<BallotViews.Row> rows(AwardCategory c) {
        Map<Integer, Long> tally = votes.tallyByNominee(c.getId());
        long total = 0;
        long top = 0;
        for (Long v : tally.values()) {
            total += v;
            top = Math.max(top, v);
        }
        List<CategoryEntry> entries = categories.entries(c.getId());
        Map<Integer, Nominee> nominees = categories.nomineesById(ids(entries));
        List<BallotViews.Row> rows = new ArrayList<>();
        Set<Integer> listed = new HashSet<>();
        for (CategoryEntry e : entries) {
            long v = tally.getOrDefault(e.getNomineeId(), 0L);
            rows.add(new BallotViews.Row(nameOf(e.getNomineeId(), entries, nominees), e.getOrganisation(), v,
                    percent(v, total), v > 0 && v == top));
            listed.add(e.getNomineeId());
        }
        for (Map.Entry<Integer, Long> t : tally.entrySet()) {
            if (!listed.contains(t.getKey())) {
                rows.add(new BallotViews.Row("Nominee #" + t.getKey() + " (removed)", null, t.getValue(),
                        percent(t.getValue(), total), t.getValue() == top));
            }
        }
        rows.sort((a, b) -> Long.compare(b.getVotes(), a.getVotes()));
        return rows;
    }

    /** Public vote count for a nominee in a category given by name (for Module 5's results page). */
    public Map<Integer, Long> tallyForCategoryName(String categoryName) {
        Optional<AwardCategory> c = categories.findActiveByName(categoryName);
        return c.isPresent() ? votes.tallyByNominee(c.get().getId()) : Collections.emptyMap();
    }

    // ------------------------------------------------------------------ helpers

    private AwardCategory activeCategory(Long id) {
        return categories.findActive(id)
                .orElseThrow(() -> new VotingException("This award category is not available for voting."));
    }

    private String uniqueReceipt() {
        String code = BallotSeal.newReceiptCode();
        int guard = 0;
        while (votes.existsByReceiptCode(code) && guard++ < 20) code = BallotSeal.newReceiptCode();
        return code;
    }

    private static Set<Integer> ids(List<CategoryEntry> entries) {
        Set<Integer> ids = new HashSet<>();
        for (CategoryEntry e : entries) ids.add(e.getNomineeId());
        return ids;
    }

    private static String nameOf(Integer nomineeId, List<CategoryEntry> entries, Map<Integer, Nominee> nominees) {
        Nominee n = nominees.get(nomineeId);
        if (n != null && n.getFullName() != null && !n.getFullName().isBlank()) return n.getFullName();
        for (CategoryEntry e : entries) if (nomineeId.equals(e.getNomineeId())) return e.getNomineeName();
        return "Nominee #" + nomineeId;
    }

    private static int percent(long part, long total) {
        if (total <= 0) return 0;
        return (int) Math.round(part * 100.0 / total);
    }

    private static boolean hasRole(User user, RoleName name) {
        if (user.getRoles() == null) return false;
        for (Role r : user.getRoles()) if (r.getName() == name) return true;
        return false;
    }
}
