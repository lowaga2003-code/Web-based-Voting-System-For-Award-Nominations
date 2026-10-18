package com.pation.awardnomination.category;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Module 2 - one eligibility rule of an award category. */
@Entity
@Table(name = "category_eligibility_rules")
public class EligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AwardCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleType ruleType;

    /** Used by MIN_BIO_WORDS. */
    @Column(name = "rule_minimum")
    private Integer minimum;

    /** Used by CUSTOM. */
    @Column(name = "rule_text", length = 300)
    private String text;

    @Column(length = 120)
    private String createdBy;

    private LocalDateTime createdAt;

    public EligibilityRule() { }

    public EligibilityRule(AwardCategory category, RuleType ruleType, Integer minimum, String text) {
        this.category = category;
        this.ruleType = ruleType;
        this.minimum = minimum;
        this.text = text;
    }

    public boolean isAutomatic() {
        return ruleType.isAutomatic();
    }

    /** Human readable wording of the rule. */
    public String describe() {
        switch (ruleType) {
            case MIN_BIO_WORDS:
                return "Biography has at least " + minimum + " words";
            case CUSTOM:
                return text;
            default:
                return ruleType.getLabel();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AwardCategory getCategory() { return category; }
    public void setCategory(AwardCategory category) { this.category = category; }

    public RuleType getRuleType() { return ruleType; }
    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }

    public Integer getMinimum() { return minimum; }
    public void setMinimum(Integer minimum) { this.minimum = minimum; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
