package com.pation.awardnomination.evaluation;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Links one Judge to one award category (many assignments per judge). */
@Entity
@Table(name = "judge_assignments")
public class JudgeAssignment extends BaseEntity {

    @ManyToOne(optional = false)
    private Judge judge;
    private String categoryName;

    protected JudgeAssignment() { }   // required by JPA
    public JudgeAssignment(Judge judge, String categoryName) {
        this.judge = judge;
        this.categoryName = categoryName;
    }

    public Judge getJudge() { return judge; }
    public String getCategoryName() { return categoryName; }
}
