package com.pation.awardnomination.evaluation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class RubricService {

    private final RubricRepository repo;
    private final EvaluationScoreRepository scoreRepo;   // [INTEGRATION] marks point to criteria

    public RubricService(RubricRepository repo, EvaluationScoreRepository scoreRepo) {
        this.repo = repo;
        this.scoreRepo = scoreRepo;
    }

    public List<Rubric> findAll() { return repo.findAll(); }

    public Rubric findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rubric " + id + " not found"));
    }

    /** [INTEGRATION - new] the rubric used to score a category (one rubric per category). */
    public Optional<Rubric> findForCategory(String categoryName) {
        if (categoryName == null) return Optional.empty();
        List<Rubric> list = repo.findByCategoryNameIgnoreCase(categoryName.trim());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Transactional
    public Rubric create(Rubric rubric) {
        rubric.setCategoryName(rubric.getCategoryName().trim());
        checkOnePerCategory(null, rubric.getCategoryName());   // [INTEGRATION]
        return repo.save(rubric);
    }

    @Transactional
    public void update(Long id, Rubric form) {
        Rubric r = findById(id);
        checkOnePerCategory(id, form.getCategoryName().trim());   // [INTEGRATION]
        r.setName(form.getName());
        r.setCategoryName(form.getCategoryName().trim());
    }

    @Transactional
    public void delete(Long id) {
        Rubric r = findById(id);
        for (RubricCriterion c : r.getCriteria()) scoreRepo.deleteByCriterionId(c.getId());   // [INTEGRATION]
        repo.delete(r);
    }

    // ---- criteria ----
    @Transactional
    public void addCriterion(Long rubricId, String name, int weight) {
        Rubric r = findById(rubricId);
        validate(name, weight);
        if (r.getTotalWeight() + weight > 100)
            throw new BusinessRuleException("Total weight would exceed 100% (currently " + r.getTotalWeight() + "%)");
        r.addCriterion(new RubricCriterion(name.trim(), weight));
    }

    @Transactional
    public void updateCriterion(Long rubricId, Long criterionId, String name, int weight) {
        Rubric r = findById(rubricId);
        RubricCriterion c = r.getCriteria().stream()
                .filter(x -> x.getId().equals(criterionId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Criterion not found"));
        validate(name, weight);
        if (r.getTotalWeight() - c.getWeight() + weight > 100)
            throw new BusinessRuleException("Total weight would exceed 100%");
        c.setName(name.trim());
        c.setWeight(weight);
    }

    @Transactional
    public void removeCriterion(Long rubricId, Long criterionId) {
        scoreRepo.deleteByCriterionId(criterionId);   // [INTEGRATION] marks for this criterion go first
        findById(rubricId).getCriteria().removeIf(c -> c.getId().equals(criterionId));
    }

    private void validate(String name, int weight) {
        if (name == null || name.isBlank()) throw new BusinessRuleException("Criterion name is required");
        if (weight < 1 || weight > 100) throw new BusinessRuleException("Weight must be between 1 and 100");
    }

    /** [INTEGRATION] the scoring page needs exactly one rubric per category. */
    private void checkOnePerCategory(Long ownId, String category) {
        for (Rubric other : repo.findByCategoryNameIgnoreCase(category)) {
            if (!other.getId().equals(ownId))
                throw new BusinessRuleException("The category \"" + category + "\" already has the rubric \""
                        + other.getName() + "\". Edit that rubric instead.");
        }
    }
}
