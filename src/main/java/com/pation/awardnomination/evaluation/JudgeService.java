package com.pation.awardnomination.evaluation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class JudgeService {

    private final JudgeRepository judgeRepo;
    private final JudgeAssignmentRepository assignRepo;
    private final ShortlistRepository shortlistRepo;
    private final List<ConflictChecker> checkers;   // POLYMORPHISM: Spring injects every implementation
    private final EvaluationScoreRepository scoreRepo;     // [INTEGRATION]
    private final EvaluationAuditLogger audit;             // [INTEGRATION] Module 4 ledger

    public JudgeService(JudgeRepository judgeRepo, JudgeAssignmentRepository assignRepo,
                        ShortlistRepository shortlistRepo, List<ConflictChecker> checkers,
                        EvaluationScoreRepository scoreRepo, EvaluationAuditLogger audit) {
        this.judgeRepo = judgeRepo;
        this.assignRepo = assignRepo;
        this.shortlistRepo = shortlistRepo;
        this.checkers = checkers;
        this.scoreRepo = scoreRepo;
        this.audit = audit;
    }

    // ---- Judge CRUD ----
    public List<Judge> findAll() { return judgeRepo.findAll(); }

    public Judge findById(Long id) {
        return judgeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Judge " + id + " not found"));
    }

    @Transactional
    public Judge create(Judge judge) {
        if (judgeRepo.findByEmail(judge.getEmail()).isPresent())
            throw new BusinessRuleException("A judge with this email already exists");
        Judge saved = judgeRepo.save(judge);
        audit.log("EVL-JUDGE-" + saved.getId(), "JUDGE_APPOINTED: " + saved.getName(),
                "Organization: " + saved.getOrganization() + ".");
        return saved;
    }

    @Transactional
    public Judge update(Long id, Judge form) {
        Judge existing = findById(id);
        judgeRepo.findByEmail(form.getEmail())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new BusinessRuleException("Another judge already uses this email"); });
        existing.setName(form.getName());
        existing.setEmail(form.getEmail());
        existing.setOrganization(form.getOrganization());
        existing.setExpertise(form.getExpertise());
        return judgeRepo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Judge judge = findById(id);
        scoreRepo.deleteByJudgeId(id);    // [INTEGRATION] the judge's marks go too
        assignRepo.deleteByJudgeId(id);   // remove child rows first
        judgeRepo.deleteById(id);
        audit.log("EVL-JUDGE-" + id, "JUDGE_DELETED: " + judge.getName(),
                "The judge, their panel places and their marks were removed.");
    }

    // ---- Assignment CRUD (with conflict check) ----
    public List<JudgeAssignment> findAllAssignments() { return assignRepo.findAll(); }

    @Transactional
    public JudgeAssignment assign(Long judgeId, String categoryName) {
        if (categoryName == null || categoryName.isBlank())
            throw new BusinessRuleException("Category is required");
        Judge judge = findById(judgeId);
        String category = categoryName.trim();
        if (assignRepo.existsByJudgeIdAndCategoryNameIgnoreCase(judgeId, category))
            throw new BusinessRuleException("Judge is already assigned to this category");

        // [INTEGRATION] winners are still candidates of the category, so they are checked as well
        for (ShortlistEntry nominee : activeEntries(category))
            for (ConflictChecker checker : checkers)
                if (checker.hasConflict(judge, nominee))
                    throw new BusinessRuleException(checker.describe(judge, nominee));

        JudgeAssignment saved = assignRepo.save(new JudgeAssignment(judge, category));
        audit.log("EVL-PANEL-" + saved.getId(), "JUDGE_ASSIGNED: " + judge.getName() + " to " + category,
                "No conflict of interest with the shortlisted candidates of this category.");
        return saved;
    }

    @Transactional
    public void removeAssignment(Long assignmentId) { removeAssignment(assignmentId, false); }

    /** [INTEGRATION] @param conflict true = removed because of a conflict of interest (written to the ledger). */
    @Transactional
    public void removeAssignment(Long assignmentId, boolean conflict) {
        JudgeAssignment a = assignRepo.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Panel place " + assignmentId + " not found"));
        // the judge's marks in this category are no longer valid
        scoreRepo.deleteByJudgeIdAndEntryCategoryNameIgnoreCase(a.getJudge().getId(), a.getCategoryName());
        assignRepo.deleteById(assignmentId);
        audit.log("EVL-PANEL-" + assignmentId,
                (conflict ? "JUDGE_REMOVED_CONFLICT: " : "JUDGE_REMOVED_FROM_PANEL: ") + a.getJudge().getName()
                        + " from " + a.getCategoryName(),
                "The judge's marks in this category were removed.");
    }

    /** [INTEGRATION - new] Conflicts that exist now (a candidate may have been shortlisted after the judge was assigned). */
    public List<PanelConflict> findConflicts() {
        List<PanelConflict> conflicts = new ArrayList<>();
        for (JudgeAssignment a : assignRepo.findAll())
            for (ShortlistEntry nominee : activeEntries(a.getCategoryName()))
                for (ConflictChecker checker : checkers)
                    if (checker.hasConflict(a.getJudge(), nominee))
                        conflicts.add(new PanelConflict(a, nominee, checker.describe(a.getJudge(), nominee)));
        return conflicts;
    }

    private List<ShortlistEntry> activeEntries(String category) {
        List<ShortlistEntry> list = new ArrayList<>();
        for (ShortlistEntry e : shortlistRepo.findByCategoryNameIgnoreCase(category)) {
            if (e.isActive()) list.add(e);
        }
        return list;
    }
}
