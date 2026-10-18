package com.pation.awardnomination.category;

import com.pation.awardnomination.nominee.Nominee;
import com.pation.awardnomination.nominee.NomineeRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Module 2 - Award Category Management (Programme Manager).
 *
 *  Create : new award categories with their eligibility rules
 *  Read   : categories, ballots and (through CategoryStatsService) real-time statistics
 *  Update : nomination and voting start / end dates, details, rules, ballot entries
 *  Delete : archive a cancelled category (soft delete; ballots are kept for the audit trail)
 *
 * Every change is written to the Module 4 transparency ledger.
 */
@Service
public class CategoryService {

    private static final Set<String> STOP_WORDS = Set.of("OF", "THE", "AND", "FOR", "A", "AN", "IN", "TO", "ON", "AT", "BY");
    private static final DateTimeFormatter LEDGER_TIME = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm", Locale.ENGLISH);
    private static final int MAX_CUSTOM_RULES = 10;

    private final AwardCategoryRepository categories;
    private final EligibilityRuleRepository rules;
    private final CategoryEntryRepository entries;
    private final NomineeRepository nominees;
    private final EligibilityChecker checker;
    private final CategoryAuditLogger audit;
    private final CategoryClock clock;
    private final ObjectProvider<BallotCounter> counters;

    public CategoryService(AwardCategoryRepository categories, EligibilityRuleRepository rules,
                           CategoryEntryRepository entries, NomineeRepository nominees,
                           EligibilityChecker checker, CategoryAuditLogger audit, CategoryClock clock,
                           ObjectProvider<BallotCounter> counters) {
        this.categories = categories;
        this.rules = rules;
        this.entries = entries;
        this.nominees = nominees;
        this.checker = checker;
        this.audit = audit;
        this.clock = clock;
        this.counters = counters;
    }

    /** A nominee that could be placed on a ballot, with the result of the eligibility check. */
    public static class Candidate {
        private final Nominee nominee;
        private final EligibilityReport report;

        Candidate(Nominee nominee, EligibilityReport report) {
            this.nominee = nominee;
            this.report = report;
        }

        public Nominee getNominee() { return nominee; }
        public EligibilityReport getReport() { return report; }
    }

    // =====================================================================================
    // Read
    // =====================================================================================

    public LocalDateTime now() {
        return clock.now();
    }

    public List<AwardCategory> activeCategories() {
        return categories.findByStatusOrderByVotingStartAsc(CategoryStatus.ACTIVE);
    }

    public List<AwardCategory> archivedCategories() {
        return categories.findByStatusOrderByArchivedAtDesc(CategoryStatus.ARCHIVED);
    }

    public AwardCategory get(Long id) {
        if (id == null) throw new CategoryException("Award category not found.");
        return categories.findById(id)
                .orElseThrow(() -> new CategoryException("Award category #" + id + " was not found."));
    }

    public Optional<AwardCategory> findActive(Long id) {
        if (id == null) return Optional.empty();
        Optional<AwardCategory> c = categories.findById(id);
        return c.isPresent() && !c.get().isArchived() ? c : Optional.empty();
    }

    /** Other modules (e.g. Module 5) refer to categories by name. */
    public Optional<AwardCategory> findActiveByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        for (AwardCategory c : categories.findByNameIgnoreCase(name.trim())) {
            if (!c.isArchived()) return Optional.of(c);
        }
        return Optional.empty();
    }

    public List<String> activeCategoryNames() {
        List<String> names = new ArrayList<>();
        for (AwardCategory c : activeCategories()) names.add(c.getName());
        return names;
    }

    public List<EligibilityRule> rules(Long categoryId) {
        return rules.findByCategoryIdOrderByIdAsc(categoryId);
    }

    public List<CategoryEntry> entries(Long categoryId) {
        return entries.findByCategoryIdOrderByNomineeNameAsc(categoryId);
    }

    public long entryCount(Long categoryId) {
        return entries.countByCategoryId(categoryId);
    }

    /** Module 3 records for the given ids (nominees deleted in Module 3 are simply missing). */
    public Map<Integer, Nominee> nomineesById(Collection<Integer> ids) {
        Map<Integer, Nominee> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) return map;
        for (Nominee n : nominees.findAllById(new ArrayList<>(ids))) map.put(n.getId(), n);
        return map;
    }

    public List<Nominee> allNominees() {
        return nominees.findAll();
    }

    public long votes(Long categoryId) {
        BallotCounter counter = counters.getIfAvailable();
        return counter == null ? 0L : counter.votesIn(categoryId);
    }

    public Map<Integer, Long> tally(Long categoryId) {
        BallotCounter counter = counters.getIfAvailable();
        return counter == null ? Collections.emptyMap() : counter.tally(categoryId);
    }

    /** The ballot is sealed once the first vote is cast or voting has closed. */
    public boolean isBallotSealed(AwardCategory c) {
        return c.phaseAt(now()) == CategoryPhase.RESULTS || votes(c.getId()) > 0;
    }

    public EligibilityReport check(Nominee nominee, Long categoryId) {
        return checker.check(nominee, rules(categoryId));
    }

    /** Module 3 nominees not yet on this ballot, each with its eligibility result. */
    public List<Candidate> candidates(Long categoryId) {
        Set<Integer> already = new HashSet<>();
        for (CategoryEntry e : entries(categoryId)) already.add(e.getNomineeId());
        List<EligibilityRule> categoryRules = rules(categoryId);
        List<Candidate> list = new ArrayList<>();
        for (Nominee n : nominees.findAll()) {
            if (n.getId() == null || already.contains(n.getId())) continue;
            list.add(new Candidate(n, checker.check(n, categoryRules)));
        }
        list.sort(Comparator.comparing((Candidate c) -> !c.getReport().isAutoPassed())
                .thenComparing(c -> nomineeName(c.getNominee()).toLowerCase(Locale.ROOT)));
        return list;
    }

    /** Snapshot of the nomination deadlines that lock Module 3 (see NominationLocks). */
    public NominationLocks locks() {
        LocalDateTime now = now();
        List<AwardCategory> active = activeCategories();
        Map<Long, AwardCategory> activeById = new HashMap<>();
        boolean anyOpen = false;
        LocalDateTime overall = null;
        for (AwardCategory c : active) {
            activeById.put(c.getId(), c);
            if (c.acceptsNominations(now)) anyOpen = true;
            overall = later(overall, c.getNominationEnd());
        }
        Map<Integer, LocalDateTime> perNominee = new HashMap<>();
        if (!active.isEmpty()) {
            for (CategoryEntry e : entries.findAll()) {
                AwardCategory c = e.getCategory() == null ? null : activeById.get(e.getCategory().getId());
                if (c != null) perNominee.merge(e.getNomineeId(), c.getNominationEnd(), CategoryService::later);
            }
        }
        return new NominationLocks(!active.isEmpty(), anyOpen, overall, perNominee);
    }

    // =====================================================================================
    // Create
    // =====================================================================================

    public AwardCategory create(CategoryForm form, String actor) {
        LocalDateTime now = now();
        String name = requireName(form.getName(), null);
        String code = isBlank(form.getCode()) ? uniqueCode(suggestCode(name)) : requireNewCode(form.getCode());
        String description = optionalText(form.getDescription(), 1000, "Description");
        Integer limit = parseLimit(form.getBallotLimit());
        Timeline t = parseTimeline(form);
        if (!t.votingEnd.isAfter(now)) {
            throw new CategoryException("The voting end must be in the future for a new category.");
        }
        Integer minWords = parseOptionalInt(form.getMinBioWords(), 1, 2000, "Minimum biography words");
        List<String> custom = customLines(form.getCustomRules());

        AwardCategory c = new AwardCategory();
        c.setName(name);
        c.setCode(code);
        c.setDescription(description);
        c.setBallotLimit(limit);
        t.applyTo(c);
        c.setCreatedBy(actor);
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        c = categories.save(c);

        List<String> ruleNames = new ArrayList<>();
        if (form.isRuleSubmitted()) ruleNames.add(saveRule(c, RuleType.SUBMITTED, null, null, actor, now));
        if (form.isRuleApproved()) ruleNames.add(saveRule(c, RuleType.APPROVED, null, null, actor, now));
        if (form.isRuleEvidence()) ruleNames.add(saveRule(c, RuleType.EVIDENCE, null, null, actor, now));
        if (form.isRuleOrganisation()) ruleNames.add(saveRule(c, RuleType.ORGANISATION, null, null, actor, now));
        if (minWords != null) ruleNames.add(saveRule(c, RuleType.MIN_BIO_WORDS, minWords, null, actor, now));
        for (String line : custom) ruleNames.add(saveRule(c, RuleType.CUSTOM, null, line, actor, now));

        audit.log(c, "CATEGORY_CREATED: " + c.getName(),
                "Nominations " + range(c.getNominationStart(), c.getNominationEnd())
                        + "; voting " + range(c.getVotingStart(), c.getVotingEnd())
                        + "; ballot limit " + (limit == null ? "none" : limit)
                        + "; eligibility: " + (ruleNames.isEmpty() ? "no rules" : String.join(", ", ruleNames)),
                actor);
        return c;
    }

    // =====================================================================================
    // Update
    // =====================================================================================

    /** Updates details and timeline. Returns what changed (empty when nothing did). */
    public List<String> update(Long id, CategoryForm form, String actor) {
        AwardCategory c = editable(id);
        LocalDateTime now = now();
        String name = requireName(form.getName(), c.getId());
        String description = optionalText(form.getDescription(), 1000, "Description");
        Integer limit = parseLimit(form.getBallotLimit());
        Timeline t = parseTimeline(form);

        boolean nominationChanged = !t.nominationStart.equals(c.getNominationStart())
                || !t.nominationEnd.equals(c.getNominationEnd());
        boolean votingStartChanged = !t.votingStart.equals(c.getVotingStart());
        boolean votingEndChanged = !t.votingEnd.equals(c.getVotingEnd());
        CategoryPhase phase = c.phaseAt(now);

        if (!name.equals(c.getName()) && phase != CategoryPhase.SCHEDULED && phase != CategoryPhase.NOMINATIONS_OPEN) {
            throw new CategoryException("The name can only be changed before nominations close, "
                    + "because other modules (judging, voting) refer to the category by its name.");
        }

        long votes = votes(c.getId());
        if (votes > 0) {
            if (phase == CategoryPhase.RESULTS && (nominationChanged || votingStartChanged || votingEndChanged)) {
                throw new CategoryException("Voting has closed with " + votes + " sealed ballot(s), so the timeline is frozen. "
                        + "Archive the category if it has to be cancelled.");
            }
            if (nominationChanged || votingStartChanged) {
                throw new CategoryException(votes + " ballot(s) are already sealed, so only the voting end can be changed.");
            }
            if (votingEndChanged && t.votingEnd.isBefore(now.truncatedTo(ChronoUnit.MINUTES))) {
                throw new CategoryException("The voting end cannot be moved into the past while ballots exist.");
            }
        }
        if (limit != null) {
            long count = entries.countByCategoryId(c.getId());
            if (count > limit) {
                throw new CategoryException("The ballot already has " + count + " nominees, so the limit cannot be " + limit + ".");
            }
        }

        List<String> changes = new ArrayList<>();
        if (!name.equals(c.getName())) changes.add("name '" + c.getName() + "' to '" + name + "'");
        if (!Objects.equals(description, c.getDescription())) changes.add("description");
        if (!Objects.equals(limit, c.getBallotLimit())) {
            changes.add("ballot limit " + show(c.getBallotLimit()) + " to " + show(limit));
        }
        dateChange(changes, "nomination start", c.getNominationStart(), t.nominationStart);
        dateChange(changes, "nomination end", c.getNominationEnd(), t.nominationEnd);
        dateChange(changes, "voting start", c.getVotingStart(), t.votingStart);
        dateChange(changes, "voting end", c.getVotingEnd(), t.votingEnd);
        if (changes.isEmpty()) return changes;

        boolean timelineChanged = nominationChanged || votingStartChanged || votingEndChanged;
        c.setName(name);
        c.setDescription(description);
        c.setBallotLimit(limit);
        t.applyTo(c);
        c.setUpdatedAt(now);
        categories.save(c);
        audit.log(c, (timelineChanged ? "TIMELINE_UPDATED: " : "DETAILS_UPDATED: ") + c.getName(),
                "Changed " + String.join("; ", changes), actor);
        return changes;
    }

    public EligibilityRule addRule(Long categoryId, String typeName, String minimum, String text, String actor) {
        AwardCategory c = editable(categoryId);
        requireBallotOpen(c, "Eligibility rules");
        RuleType type = parseRuleType(typeName);
        Integer min = null;
        String wording = null;
        if (type == RuleType.MIN_BIO_WORDS) {
            min = parseOptionalInt(minimum, 1, 2000, "Minimum biography words");
            if (min == null) throw new CategoryException("Enter the minimum number of biography words.");
        }
        if (type == RuleType.CUSTOM) {
            wording = text == null ? "" : text.trim().replaceAll("\\s+", " ");
            if (wording.length() < 3 || wording.length() > 300) {
                throw new CategoryException("Write the custom rule in 3 to 300 characters.");
            }
        }
        if (type != RuleType.CUSTOM) {
            for (EligibilityRule r : rules(categoryId)) {
                if (r.getRuleType() == type) {
                    throw new CategoryException("This category already has the rule \"" + r.describe() + "\".");
                }
            }
        }
        EligibilityRule rule = new EligibilityRule(c, type, min, wording);
        rule.setCreatedBy(actor);
        rule.setCreatedAt(now());
        rule = rules.save(rule);
        audit.log(c, "RULE_ADDED: " + c.getName(),
                rule.describe() + (rule.isAutomatic() ? " (checked automatically)" : " (checked by the Programme Manager)"), actor);
        return rule;
    }

    public void removeRule(Long categoryId, Long ruleId, String actor) {
        AwardCategory c = editable(categoryId);
        requireBallotOpen(c, "Eligibility rules");
        EligibilityRule rule = rules.findById(ruleId)
                .filter(r -> r.getCategory() != null && categoryId.equals(r.getCategory().getId()))
                .orElseThrow(() -> new CategoryException("That rule does not belong to this category."));
        rules.delete(rule);
        audit.log(c, "RULE_REMOVED: " + c.getName(), rule.describe(), actor);
    }

    /** Places a Module 3 nominee on the category's ballot after the eligibility check. */
    public CategoryEntry addEntry(Long categoryId, Integer nomineeId, boolean manualConfirmed, String actor) {
        AwardCategory c = editable(categoryId);
        requireBallotOpen(c, "Ballot entries");
        if (nomineeId == null) throw new CategoryException("Choose a nominee to place on the ballot.");
        Nominee n = nominees.findById(nomineeId)
                .orElseThrow(() -> new CategoryException("Nominee #" + nomineeId + " was not found in Module 3."));
        String displayName = nomineeName(n);
        if (entries.existsByCategoryIdAndNomineeId(categoryId, nomineeId)) {
            throw new CategoryException(displayName + " is already on this ballot.");
        }
        if (c.getBallotLimit() != null && entries.countByCategoryId(categoryId) >= c.getBallotLimit()) {
            throw new CategoryException("The ballot is full (limit " + c.getBallotLimit() + " nominees).");
        }
        EligibilityReport report = checker.check(n, rules(categoryId));
        if (!report.isAutoPassed()) {
            throw new CategoryException(displayName + " is not eligible: " + report.failedRules() + ".");
        }
        if (report.hasManualRules() && !manualConfirmed) {
            throw new CategoryException("Tick the box to confirm the manual eligibility rules for " + displayName + ".");
        }
        CategoryEntry entry = new CategoryEntry(c, n.getId(), displayName, n.getCompanyName());
        entry.setEligibilityNote(report.summary(manualConfirmed));
        entry.setAddedBy(actor);
        entry.setAddedAt(now());
        entry = entries.save(entry);
        audit.log(c, "ENTRY_ADDED: " + c.getName(),
                entry.getNomineeName() + " placed on the ballot (" + entry.getEligibilityNote() + ")", actor);
        return entry;
    }

    public void removeEntry(Long categoryId, Long entryId, String actor) {
        AwardCategory c = editable(categoryId);
        requireBallotOpen(c, "Ballot entries");
        CategoryEntry entry = entries.findById(entryId)
                .filter(e -> e.getCategory() != null && categoryId.equals(e.getCategory().getId()))
                .orElseThrow(() -> new CategoryException("That nominee is not on this ballot."));
        entries.delete(entry);
        audit.log(c, "ENTRY_REMOVED: " + c.getName(), entry.getNomineeName() + " removed from the ballot", actor);
    }

    // =====================================================================================
    // Delete (archive)
    // =====================================================================================

    public void archive(Long id, String reason, String actor) {
        AwardCategory c = get(id);
        if (c.isArchived()) throw new CategoryException("'" + c.getName() + "' is already archived.");
        String why = reason == null ? "" : reason.trim().replaceAll("\\s+", " ");
        if (why.length() < 5 || why.length() > 500) {
            throw new CategoryException("Give a reason for cancelling this category (5 to 500 characters).");
        }
        long votes = votes(c.getId());
        CategoryPhase before = c.phaseAt(now());
        c.archive(why, actor, now());
        c.setUpdatedAt(now());
        categories.save(c);
        audit.log(c, "CATEGORY_ARCHIVED: " + c.getName(),
                "Cancelled during phase " + before.getLabel() + ". Reason: " + why
                        + (votes > 0 ? ". " + votes + " sealed ballot(s) are kept for the audit trail." : "."), actor);
    }

    public void restore(Long id, String actor) {
        AwardCategory c = get(id);
        if (!c.isArchived()) throw new CategoryException("'" + c.getName() + "' is not archived.");
        for (AwardCategory other : categories.findByNameIgnoreCase(c.getName())) {
            if (!other.isArchived() && !other.getId().equals(c.getId())) {
                throw new CategoryException("An active category called '" + other.getName() + "' already exists, so this one cannot be restored.");
            }
        }
        c.restore();
        c.setUpdatedAt(now());
        categories.save(c);
        audit.log(c, "CATEGORY_RESTORED: " + c.getName(), "Back on the timeline, now in phase " + c.phaseAt(now()).getLabel(), actor);
    }

    /** Permanent removal is only allowed for an archived category that never received a vote. */
    public void deletePermanently(Long id, String actor) {
        AwardCategory c = get(id);
        if (!c.isArchived()) throw new CategoryException("Archive the category first; only archived categories can be deleted.");
        long votes = votes(c.getId());
        if (votes > 0) {
            throw new CategoryException("'" + c.getName() + "' has " + votes + " sealed ballot(s). "
                    + "Categories with ballots stay archived so the audit trail is complete.");
        }
        List<CategoryEntry> categoryEntries = entries(c.getId());
        List<EligibilityRule> categoryRules = rules(c.getId());
        entries.deleteAll(categoryEntries);
        rules.deleteAll(categoryRules);
        categories.delete(c);
        audit.log(c, "CATEGORY_DELETED: " + c.getName(),
                "Archived category removed permanently with " + categoryRules.size() + " rule(s) and "
                        + categoryEntries.size() + " ballot entr" + (categoryEntries.size() == 1 ? "y" : "ies"), actor);
    }

    // =====================================================================================
    // helpers
    // =====================================================================================

    private AwardCategory editable(Long id) {
        AwardCategory c = get(id);
        if (c.isArchived()) {
            throw new CategoryException("'" + c.getName() + "' is archived and read-only. Restore it first.");
        }
        return c;
    }

    private void requireBallotOpen(AwardCategory c, String what) {
        if (c.phaseAt(now()) == CategoryPhase.RESULTS) {
            throw new CategoryException(what + " are frozen because voting for '" + c.getName() + "' has closed.");
        }
        long votes = votes(c.getId());
        if (votes > 0) {
            throw new CategoryException(what + " are frozen: " + votes + " vote(s) have already been cast in '" + c.getName() + "'.");
        }
    }

    private String saveRule(AwardCategory c, RuleType type, Integer min, String text, String actor, LocalDateTime now) {
        EligibilityRule rule = new EligibilityRule(c, type, min, text);
        rule.setCreatedBy(actor);
        rule.setCreatedAt(now);
        rules.save(rule);
        return rule.describe();
    }

    private String requireName(String raw, Long selfId) {
        String name = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (name.length() < 3) throw new CategoryException("The category name needs at least 3 characters.");
        if (name.length() > 120) throw new CategoryException("The category name can have at most 120 characters.");
        for (AwardCategory other : categories.findByNameIgnoreCase(name)) {
            if (!other.isArchived() && !other.getId().equals(selfId)) {
                throw new CategoryException("An active category called '" + other.getName() + "' already exists.");
            }
        }
        return name;
    }

    private String requireNewCode(String raw) {
        String code = raw.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "-");
        if (!code.matches("[A-Z0-9][A-Z0-9-]{1,15}")) {
            throw new CategoryException("The code must be 2 to 16 letters, digits or dashes, e.g. BEST-INNOVATOR.");
        }
        if (categories.existsByCode(code)) {
            throw new CategoryException("The code " + code + " is already used by another category.");
        }
        return code;
    }

    /** "Innovator of the Year" becomes INNOVATOR-YEAR; long names are shortened. */
    static String suggestCode(String name) {
        String[] words = name.toUpperCase(Locale.ROOT).replaceAll("['\u2019]", "").replaceAll("[^A-Z0-9 ]", " ").trim().split("\\s+");
        List<String> keep = new ArrayList<>();
        for (String w : words) {
            if (!w.isEmpty() && !STOP_WORDS.contains(w)) keep.add(w);
        }
        if (keep.isEmpty()) keep.add("AWARD");
        String joined = String.join("-", keep);
        if (joined.length() <= 16) return joined.length() >= 2 ? joined : joined + "-AWARD";
        String first = keep.get(0).length() > 10 ? keep.get(0).substring(0, 10) : keep.get(0);
        StringBuilder sb = new StringBuilder(first);
        if (keep.size() > 1) {
            sb.append('-');
            for (int i = 1; i < keep.size() && sb.length() < 16; i++) sb.append(keep.get(i).charAt(0));
        }
        return sb.length() > 16 ? sb.substring(0, 16) : sb.toString();
    }

    private String uniqueCode(String base) {
        String code = base;
        int n = 2;
        while (categories.existsByCode(code)) {
            String suffix = "-" + n++;
            String head = base.length() + suffix.length() > 16 ? base.substring(0, 16 - suffix.length()) : base;
            code = head + suffix;
        }
        return code;
    }

    private static String optionalText(String raw, int max, String label) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        if (s.length() > max) throw new CategoryException(label + " can have at most " + max + " characters.");
        return s;
    }

    private static Integer parseLimit(String raw) {
        return parseOptionalInt(raw, 2, 50, "The ballot limit");
    }

    private static Integer parseOptionalInt(String raw, int min, int max, String label) {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            int v = Integer.parseInt(raw.trim());
            if (v < min || v > max) throw new CategoryException(label + " must be between " + min + " and " + max + ".");
            return v;
        } catch (NumberFormatException e) {
            throw new CategoryException(label + " must be a whole number.");
        }
    }

    private static List<String> customLines(String raw) {
        List<String> lines = new ArrayList<>();
        if (raw == null) return lines;
        for (String line : raw.split("\\r?\\n")) {
            String s = line.trim().replaceAll("\\s+", " ");
            if (s.isEmpty()) continue;
            if (s.length() < 3 || s.length() > 300) {
                throw new CategoryException("Each custom rule needs 3 to 300 characters: \"" + s + "\"");
            }
            lines.add(s);
        }
        if (lines.size() > MAX_CUSTOM_RULES) throw new CategoryException("Add at most " + MAX_CUSTOM_RULES + " custom rules.");
        return lines;
    }

    private static RuleType parseRuleType(String raw) {
        try {
            return RuleType.valueOf(raw == null ? "" : raw.trim());
        } catch (IllegalArgumentException e) {
            throw new CategoryException("Choose a valid rule type.");
        }
    }

    /** The four dates of a timeline, already checked to be in the right order. */
    static final class Timeline {
        LocalDateTime nominationStart;
        LocalDateTime nominationEnd;
        LocalDateTime votingStart;
        LocalDateTime votingEnd;

        void applyTo(AwardCategory c) {
            c.setNominationStart(nominationStart);
            c.setNominationEnd(nominationEnd);
            c.setVotingStart(votingStart);
            c.setVotingEnd(votingEnd);
        }
    }

    static Timeline parseTimeline(CategoryForm f) {
        Timeline t = new Timeline();
        t.nominationStart = parseDate(f.getNominationStart(), "Nomination start");
        t.nominationEnd = parseDate(f.getNominationEnd(), "Nomination end");
        t.votingStart = parseDate(f.getVotingStart(), "Voting start");
        t.votingEnd = parseDate(f.getVotingEnd(), "Voting end");
        if (!t.nominationEnd.isAfter(t.nominationStart)) {
            throw new CategoryException("The nomination end must be after the nomination start.");
        }
        if (t.votingStart.isBefore(t.nominationEnd)) {
            throw new CategoryException("Voting can only start after nominations close (voting start must be on or after the nomination end).");
        }
        if (!t.votingEnd.isAfter(t.votingStart)) {
            throw new CategoryException("The voting end must be after the voting start.");
        }
        return t;
    }

    private static LocalDateTime parseDate(String raw, String label) {
        if (raw == null || raw.trim().isEmpty()) throw new CategoryException(label + " is required.");
        try {
            LocalDateTime d = LocalDateTime.parse(raw.trim());
            if (d.getYear() < 2000 || d.getYear() > 2100) throw new CategoryException(label + " must be between the years 2000 and 2100.");
            return d.withSecond(0).withNano(0);
        } catch (DateTimeParseException e) {
            throw new CategoryException(label + " is not a valid date and time.");
        }
    }

    private static void dateChange(List<String> changes, String label, LocalDateTime before, LocalDateTime after) {
        if (!after.equals(before)) changes.add(label + " " + LEDGER_TIME.format(before) + " to " + LEDGER_TIME.format(after));
    }

    static String range(LocalDateTime from, LocalDateTime to) {
        return LEDGER_TIME.format(from) + " to " + LEDGER_TIME.format(to);
    }

    private static String show(Integer limit) {
        return limit == null ? "none" : String.valueOf(limit);
    }

    static LocalDateTime later(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return b.isAfter(a) ? b : a;
    }

    static String nomineeName(Nominee n) {
        String name = n.getFullName();
        return name == null || name.isBlank() ? "Nominee #" + n.getId() : name.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
