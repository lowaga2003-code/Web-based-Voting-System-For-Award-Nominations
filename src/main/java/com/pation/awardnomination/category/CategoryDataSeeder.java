package com.pation.awardnomination.category;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Module 2 demo data. Runs only when the award_categories table is empty and creates three
 * categories whose dates are relative to the first start-up, so every phase can be shown:
 *   - Community Impact Award : voting is live now (try it with voter1@test.com)
 *   - Innovator of the Year  : nominations are open now
 *   - Young Achiever Award   : scheduled (nominations open in a few days)
 * No nominee is placed on a ballot automatically: the Programme Manager does that from the
 * category page, so Module 3 profiles are not locked until someone chooses to.
 */
@Component
@Order(60)
public class CategoryDataSeeder implements CommandLineRunner {

    private final AwardCategoryRepository categories;
    private final EligibilityRuleRepository rules;
    private final CategoryAuditLogger audit;
    private final CategoryClock clock;

    public CategoryDataSeeder(AwardCategoryRepository categories, EligibilityRuleRepository rules,
                              CategoryAuditLogger audit, CategoryClock clock) {
        this.categories = categories;
        this.rules = rules;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (categories.count() > 0) return;
        LocalDateTime now = clock.now().truncatedTo(ChronoUnit.HOURS);

        AwardCategory impact = category("Community Impact Award", "COMMUNITY-IMPACT",
                "Honours a person or team whose work made a measurable difference to a local community.",
                10, now.minusDays(21), now.minusDays(1), now.minusDays(1), now.plusDays(21));
        rule(impact, RuleType.SUBMITTED, null, null);
        rule(impact, RuleType.ORGANISATION, null, null);

        AwardCategory innovator = category("Innovator of the Year", "INNOVATOR-YEAR",
                "For a new product, service or idea that changed how people work or live this year.",
                8, now.minusDays(2), now.plusDays(14), now.plusDays(15), now.plusDays(30));
        rule(innovator, RuleType.SUBMITTED, null, null);
        rule(innovator, RuleType.EVIDENCE, null, null);
        rule(innovator, RuleType.MIN_BIO_WORDS, 20, null);

        AwardCategory young = category("Young Achiever Award", "YOUNG-ACHIEVER",
                "Celebrates an outstanding achiever at the start of their career.",
                null, now.plusDays(3), now.plusDays(20), now.plusDays(21), now.plusDays(35));
        rule(young, RuleType.SUBMITTED, null, null);
        rule(young, RuleType.APPROVED, null, null);
        rule(young, RuleType.CUSTOM, null, "Nominee is 35 years old or younger on the nomination closing date");

        for (AwardCategory c : List.of(impact, innovator, young)) {
            audit.system(CategoryAuditLogger.ticketCode(c), "CATEGORY_CREATED: " + c.getName(),
                    "Demo category created at first start-up. Nominations " + CategoryService.range(c.getNominationStart(), c.getNominationEnd())
                            + "; voting " + CategoryService.range(c.getVotingStart(), c.getVotingEnd()));
        }
        System.out.println("[Module 2] Demo award categories created. Place nominees on the Community Impact Award ballot to start voting.");
    }

    private AwardCategory category(String name, String code, String description, Integer limit,
                                   LocalDateTime ns, LocalDateTime ne, LocalDateTime vs, LocalDateTime ve) {
        AwardCategory c = new AwardCategory();
        c.setName(name);
        c.setCode(code);
        c.setDescription(description);
        c.setBallotLimit(limit);
        c.setNominationStart(ns);
        c.setNominationEnd(ne);
        c.setVotingStart(vs);
        c.setVotingEnd(ve);
        c.setCreatedBy("demo data");
        c.setCreatedAt(clock.now());
        c.setUpdatedAt(clock.now());
        return categories.save(c);
    }

    private void rule(AwardCategory c, RuleType type, Integer min, String text) {
        EligibilityRule r = new EligibilityRule(c, type, min, text);
        r.setCreatedBy("demo data");
        r.setCreatedAt(clock.now());
        rules.save(r);
    }
}
