package com.pation.awardnomination.evaluation;

import com.pation.awardnomination.category.AwardCategory;
import com.pation.awardnomination.category.CategoryPhase;
import com.pation.awardnomination.category.CategoryService;
import com.pation.awardnomination.nominee.Interview;
import com.pation.awardnomination.nominee.InterviewRepository;
import com.pation.awardnomination.nominee.Nominee;
import com.pation.awardnomination.nominee.NomineeRepository;
import com.pation.awardnomination.voting.VotingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * [INTEGRATION - new] The parts of the specification that were not built yet:
 *  - Read  : a shortlisted candidate's Module 3 evidence + interview slot
 *  - Update: judges' marks against the category's rubric (re-evaluation changes them) and the "Winner" status
 *
 * (සිංහලෙන්: Chairperson, panel එකේ ඉන්න judge ගේ ලකුණු (0-10) rubric එකේ හැම criterion එකකටම දානවා.
 *  Weight එක අනුව 100න් total එක හැදෙනවා, judges ලාගේ average එකෙන් rank කරනවා, ඊට පස්සේ Winner තෝරනවා.)
 */
@Service
public class ScoringService {

    private final ShortlistService shortlist;
    private final ShortlistRepository shortlistRepo;
    private final JudgeService judges;
    private final JudgeAssignmentRepository assignRepo;
    private final RubricService rubrics;
    private final EvaluationScoreRepository scoreRepo;
    private final List<ConflictChecker> checkers;
    private final NomineeRepository nominees;        // Module 3 (read only)
    private final InterviewRepository interviews;    // Module 3 interview scheduling (read only)
    private final EvaluationAuditLogger audit;
    private final CategoryService awardCategories;   // [INTEGRATION v5] Module 2 categories + timeline (read only)
    private final VotingService voting;              // [INTEGRATION v5] public vote counts (read only)

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH);

    public ScoringService(ShortlistService shortlist, ShortlistRepository shortlistRepo, JudgeService judges,
                          JudgeAssignmentRepository assignRepo, RubricService rubrics, EvaluationScoreRepository scoreRepo,
                          List<ConflictChecker> checkers, NomineeRepository nominees, InterviewRepository interviews,
                          EvaluationAuditLogger audit, CategoryService awardCategories, VotingService voting) {
        this.shortlist = shortlist;
        this.shortlistRepo = shortlistRepo;
        this.judges = judges;
        this.assignRepo = assignRepo;
        this.rubrics = rubrics;
        this.scoreRepo = scoreRepo;
        this.checkers = checkers;
        this.nominees = nominees;
        this.interviews = interviews;
        this.audit = audit;
        this.awardCategories = awardCategories;
        this.voting = voting;
    }

    // ---------------------------------------------------------------- read

    public Optional<Rubric> rubricFor(ShortlistEntry entry) {
        return rubrics.findForCategory(entry.getCategoryName());
    }

    /** Judges on the panel of a category (Judge Panel page). */
    public List<Judge> panel(String category) {
        Map<Long, Judge> map = new LinkedHashMap<>();
        for (JudgeAssignment a : assignRepo.findByCategoryNameIgnoreCase(category)) {
            map.put(a.getJudge().getId(), a.getJudge());
        }
        return new ArrayList<>(map.values());
    }

    /** One score sheet per judge on the panel. */
    public List<ScoreSheet> sheets(ShortlistEntry entry) {
        Rubric rubric = rubricFor(entry).orElse(null);
        Map<Long, ScoreSheet> map = new LinkedHashMap<>();
        for (Judge j : panel(entry.getCategoryName())) {
            map.put(j.getId(), new ScoreSheet(j));
        }
        for (EvaluationScore s : scoreRepo.findByEntryId(entry.getId())) {
            ScoreSheet sheet = map.get(s.getJudge().getId());
            if (sheet == null) {
                sheet = new ScoreSheet(s.getJudge());
                map.put(s.getJudge().getId(), sheet);
            }
            sheet.add(s);
        }
        List<ScoreSheet> list = new ArrayList<>(map.values());
        for (ScoreSheet sheet : list) {
            sheet.calculate(rubric);
        }
        return list;
    }

    /** Average of the complete score sheets (out of 100), or null if no judge has finished. */
    public static Double average(List<ScoreSheet> sheets) {
        double sum = 0;
        int n = 0;
        for (ScoreSheet s : sheets) {
            if (s.isComplete()) {
                sum += s.getTotal();
                n++;
            }
        }
        return n == 0 ? null : Math.round(sum / n * 10) / 10.0;
    }

    /** Module 3 nomination behind a shortlist entry (biography, evidence file, status ...). */
    public Optional<Nominee> nominee(ShortlistEntry entry) {
        return entry.getNomineeId() == null ? Optional.empty() : nominees.findById(entry.getNomineeId().intValue());
    }

    /** The interview slot the nominee booked in Module 3 (null = none). */
    public Interview interview(Nominee nominee) {
        if (nominee == null || nominee.getNic() == null) {
            return null;
        }
        try {
            return interviews.findByNomineeNic(nominee.getNic());
        } catch (RuntimeException e) {   // more than one slot booked with the same NIC
            return null;
        }
    }

    /** Candidates of a category ranked by their average mark; disqualified candidates last. */
    public List<CandidateResult> results(String category) {
        List<CandidateResult> active = new ArrayList<>();
        List<CandidateResult> out = new ArrayList<>();
        int panelSize = panel(category).size();
        for (ShortlistEntry e : shortlistRepo.findByCategoryNameIgnoreCase(category)) {
            List<ScoreSheet> sheets = sheets(e);
            int complete = 0;
            for (ScoreSheet s : sheets) {
                if (s.isComplete()) complete++;
            }
            CandidateResult r = new CandidateResult(e, average(sheets), complete, panelSize);
            (e.isActive() ? active : out).add(r);
        }
        active.sort((a, b) -> {
            if (a.getAverage() == null && b.getAverage() == null) return 0;
            if (a.getAverage() == null) return 1;
            if (b.getAverage() == null) return -1;
            return Double.compare(b.getAverage(), a.getAverage());
        });
        int rank = 0;
        for (CandidateResult r : active) {
            if (r.getAverage() != null) r.setRank(++rank);
        }
        List<CandidateResult> all = new ArrayList<>(active);
        all.addAll(out);
        // [INTEGRATION v5] public votes next to the judges' average, once Module 2 says voting has closed
        Map<Integer, Long> votes = publicVotes(category);
        if (!votes.isEmpty()) {
            long total = 0;
            for (Long v : votes.values()) total += v;
            for (CandidateResult r : all) {
                Long nomineeId = r.getEntry().getNomineeId();
                if (nomineeId != null) r.setPublicVotes(votes.getOrDefault(nomineeId.intValue(), 0L), total);
            }
        }
        return all;
    }

    // ---------------------------------------------------------------- [INTEGRATION v5] Module 2 + Voting

    /** The Programme Manager's Module 2 category with this name (empty = a free-text Module 5 category). */
    public Optional<AwardCategory> awardCategory(String category) {
        return awardCategories.findActiveByName(category);
    }

    /** Where the Module 2 category is on its timeline, or null when the category is not in Module 2. */
    public CategoryPhase votingPhase(String category) {
        Optional<AwardCategory> c = awardCategory(category);
        return c.isPresent() ? c.get().phaseAt(awardCategories.now()) : null;
    }

    /** Public votes per Module 3 nominee id. Published only after the category's voting window has closed. */
    public Map<Integer, Long> publicVotes(String category) {
        if (votingPhase(category) != CategoryPhase.RESULTS) return Collections.emptyMap();
        return voting.tallyForCategoryName(category);
    }

    /** Every category used in the judge panel, the rubrics or the shortlist, plus the active Module 2 categories. */
    public List<String> categories() {
        Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String name : awardCategories.activeCategoryNames()) map.putIfAbsent(name, name);   // [INTEGRATION v5]
        for (JudgeAssignment a : assignRepo.findAll()) map.putIfAbsent(a.getCategoryName(), a.getCategoryName());
        for (Rubric r : rubrics.findAll()) map.putIfAbsent(r.getCategoryName(), r.getCategoryName());
        for (ShortlistEntry e : shortlistRepo.findAll()) map.putIfAbsent(e.getCategoryName(), e.getCategoryName());
        return new ArrayList<>(map.values());
    }

    // ---------------------------------------------------------------- update

    /**
     * Saves one judge's marks for a candidate. Changing an existing mark is a re-evaluation.
     * @param marks criterion id -> mark (0-10), every criterion of the rubric is required
     * @return true if existing marks were changed (re-evaluation)
     */
    @Transactional
    public boolean saveScores(Long entryId, Long judgeId, Map<Long, Integer> marks) {
        ShortlistEntry entry = shortlist.findById(entryId);
        if (!entry.isActive())
            throw new BusinessRuleException("A disqualified candidate can't be scored. Reinstate the candidate first.");
        Rubric rubric = rubricFor(entry).orElseThrow(() -> new BusinessRuleException(
                "Create a rubric for \"" + entry.getCategoryName() + "\" on the Rubrics page first."));
        if (!rubric.isComplete())
            throw new BusinessRuleException("The rubric \"" + rubric.getName() + "\" adds up to " + rubric.getTotalWeight()
                    + "%. It must add up to 100% before marks can be given.");
        Judge judge = judges.findById(judgeId);
        if (!assignRepo.existsByJudgeIdAndCategoryNameIgnoreCase(judgeId, entry.getCategoryName()))
            throw new BusinessRuleException(judge.getName() + " is not on the panel of \"" + entry.getCategoryName() + "\".");
        for (ConflictChecker checker : checkers)
            if (checker.hasConflict(judge, entry))
                throw new BusinessRuleException(checker.describe(judge, entry));

        Map<Long, EvaluationScore> existing = new HashMap<>();
        for (EvaluationScore s : scoreRepo.findByEntryIdAndJudgeId(entryId, judgeId)) {
            existing.put(s.getCriterion().getId(), s);
        }
        int changed = 0;
        for (RubricCriterion c : rubric.getCriteria()) {
            Integer mark = marks == null ? null : marks.get(c.getId());
            if (mark == null)
                throw new BusinessRuleException("Give a mark for \"" + c.getName() + "\".");
            if (mark < 0 || mark > EvaluationScore.MAX_SCORE)
                throw new BusinessRuleException("Marks must be between 0 and " + EvaluationScore.MAX_SCORE + ".");
        }
        for (RubricCriterion c : rubric.getCriteria()) {
            int mark = marks.get(c.getId());
            EvaluationScore s = existing.get(c.getId());
            if (s == null) {
                scoreRepo.save(new EvaluationScore(entry, judge, c, mark));
            } else if (s.change(mark)) {
                scoreRepo.save(s);
                changed++;
            }
        }
        boolean reEvaluation = !existing.isEmpty();
        if (!reEvaluation || changed > 0) {
            audit.log("EVL-ENTRY-" + entryId,
                    (reEvaluation ? "RE_EVALUATED: " : "SCORES_RECORDED: ") + "Nominee #" + entry.getNomineeId()
                            + " in " + entry.getCategoryName() + " by judge " + judge.getName(),
                    reEvaluation ? changed + " mark(s) changed." : "Rubric: " + rubric.getName() + ".");
        }
        return reEvaluation && changed > 0;
    }

    /** Specification "Update: change a candidate's status to Winner". One winner per category. */
    @Transactional
    public ShortlistEntry markWinner(Long entryId) {
        ShortlistEntry entry = shortlist.findById(entryId);
        if (entry.getStatus() == ShortlistStatus.WINNER)
            throw new BusinessRuleException("This candidate is already the winner.");
        if (entry.getStatus() != ShortlistStatus.SHORTLISTED)
            throw new BusinessRuleException("A disqualified candidate can't be the winner.");
        if (average(sheets(entry)) == null)
            throw new BusinessRuleException("At least one judge must finish scoring this candidate first.");
        // [INTEGRATION v5] Module 2 is the time engine: winners are chosen after the public vote has closed
        CategoryPhase phase = votingPhase(entry.getCategoryName());
        if (phase != null && phase != CategoryPhase.RESULTS) {
            AwardCategory c = awardCategory(entry.getCategoryName()).get();
            throw new BusinessRuleException("Public voting for \"" + c.getName() + "\" closes on " + WHEN.format(c.getVotingEnd())
                    + " (Module 2 timeline). The winner can be selected after voting has closed.");
        }
        for (ShortlistEntry other : shortlistRepo.findByCategoryNameIgnoreCaseAndStatus(entry.getCategoryName(), ShortlistStatus.WINNER)) {
            other.revokeWinner();
            shortlistRepo.save(other);
            audit.log("EVL-ENTRY-" + other.getId(), "WINNER_REVOKED: Nominee #" + other.getNomineeId() + " in "
                    + other.getCategoryName(), "Replaced by another winner.");
        }
        entry.markWinner();
        ShortlistEntry saved = shortlistRepo.save(entry);
        audit.log("EVL-ENTRY-" + entryId, "WINNER_SELECTED: Nominee #" + entry.getNomineeId() + " "
                + entry.getNomineeName() + " in " + entry.getCategoryName(), "Selected by the Selection Committee Chairperson.");
        return saved;
    }

    @Transactional
    public ShortlistEntry revokeWinner(Long entryId) {
        ShortlistEntry entry = shortlist.findById(entryId);
        if (entry.getStatus() != ShortlistStatus.WINNER)
            throw new BusinessRuleException("This candidate is not the winner.");
        entry.revokeWinner();
        ShortlistEntry saved = shortlistRepo.save(entry);
        audit.log("EVL-ENTRY-" + entryId, "WINNER_REVOKED: Nominee #" + entry.getNomineeId() + " in " + entry.getCategoryName(), "");
        return saved;
    }

    // ---------------------------------------------------------------- overview (home page)

    public Map<String, Long> overview() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("judges", (long) judges.findAll().size());
        m.put("panelPlaces", (long) assignRepo.count());
        long complete = 0;
        List<Rubric> all = rubrics.findAll();
        for (Rubric r : all) {
            if (r.isComplete()) complete++;
        }
        m.put("rubrics", (long) all.size());
        m.put("completeRubrics", complete);
        m.put("shortlisted", shortlistRepo.countByStatus(ShortlistStatus.SHORTLISTED));
        m.put("disqualified", shortlistRepo.countByStatus(ShortlistStatus.DISQUALIFIED));
        m.put("winners", shortlistRepo.countByStatus(ShortlistStatus.WINNER));
        m.put("marks", scoreRepo.count());
        m.put("conflicts", (long) judges.findConflicts().size());
        return m;
    }
}
