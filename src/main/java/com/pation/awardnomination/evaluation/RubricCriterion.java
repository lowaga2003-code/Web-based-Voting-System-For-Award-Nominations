package com.pation.awardnomination.evaluation;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rubric_criteria")
public class RubricCriterion extends BaseEntity {

    private String name;
    private int weight;          // percentage, 1-100

    @ManyToOne
    private Rubric rubric;

    protected RubricCriterion() { }
    public RubricCriterion(String name, int weight) { this.name = name; this.weight = weight; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    // ENCAPSULATION: package-private, only Rubric (same package) may attach a criterion
    void setRubric(Rubric rubric) { this.rubric = rubric; }
}
