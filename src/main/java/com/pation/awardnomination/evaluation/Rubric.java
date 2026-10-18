package com.pation.awardnomination.evaluation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rubrics")
public class Rubric extends BaseEntity {

    @NotBlank(message = "Rubric name is required")
    private String name;
    @NotBlank(message = "Category is required")
    private String categoryName;

    @OneToMany(mappedBy = "rubric", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RubricCriterion> criteria = new ArrayList<>();

    /** Business logic lives inside the object that owns the data (ENCAPSULATION). */
    public int getTotalWeight() {
        return criteria.stream().mapToInt(RubricCriterion::getWeight).sum();
    }

    /** [INTEGRATION] only a rubric whose weights add up to 100% can be used for scoring. */
    public boolean isComplete() { return getTotalWeight() == 100; }

    public void addCriterion(RubricCriterion c) {
        c.setRubric(this);
        criteria.add(c);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public List<RubricCriterion> getCriteria() { return criteria; }
}
