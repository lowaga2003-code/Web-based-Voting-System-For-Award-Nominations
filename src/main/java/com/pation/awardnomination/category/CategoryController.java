package com.pation.awardnomination.category;

import com.pation.awardnomination.admin.User;
import com.pation.awardnomination.admin.UserRepository;
import com.pation.awardnomination.dispute.AuditTransparencyLog;
import com.pation.awardnomination.dispute.AuditTransparencyLogRepository;
import com.pation.awardnomination.nominee.Nominee;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Module 2 - Award Category Management pages for the Programme Manager.
 * Everything lives under /manager/awards, which SecurityConfig opens to the Programme Manager
 * and the System Administrator.
 */
@Controller
@RequestMapping("/manager/awards")
public class CategoryController {

    private static final String OK = "cmOk";
    private static final String ERROR = "cmError";

    private final CategoryService categoryService;
    private final CategoryStatsService statsService;
    private final AuditTransparencyLogRepository ledger;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService, CategoryStatsService statsService,
                              AuditTransparencyLogRepository ledger, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.statsService = statsService;
        this.ledger = ledger;
        this.userRepository = userRepository;
    }

    // ------------------------------------------------------------------ Read

    /** Control board: departure board, season timeline, attention list, recent ledger lines. */
    @GetMapping
    public String board(Model model, Principal principal) {
        LocalDateTime now = categoryService.now();
        LiveStats stats = statsService.snapshot();
        model.addAttribute("stats", stats);
        model.addAttribute("now", now);
        model.addAttribute("notices", notices(stats, now));
        model.addAttribute("recent", ledgerLines(null, 8));
        model.addAttribute("locks", categoryService.locks());
        model.addAttribute("actor", actor(principal));
        return "category/board";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, Principal principal, RedirectAttributes flash) {
        AwardCategory c;
        try {
            c = categoryService.get(id);
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/manager/awards";
        }
        LocalDateTime now = categoryService.now();
        List<CategoryEntry> entries = categoryService.entries(id);
        Set<Integer> ids = new HashSet<>();
        for (CategoryEntry e : entries) ids.add(e.getNomineeId());
        Map<Integer, Nominee> nominees = categoryService.nomineesById(ids);
        Map<Integer, EligibilityReport> reports = new HashMap<>();
        for (CategoryEntry e : entries) {
            Nominee n = nominees.get(e.getNomineeId());
            if (n != null) reports.put(e.getNomineeId(), categoryService.check(n, id));
        }
        boolean sealed = c.isArchived() || categoryService.isBallotSealed(c);
        model.addAttribute("category", c);
        model.addAttribute("phase", c.phaseAt(now));
        model.addAttribute("now", now);
        model.addAttribute("rules", categoryService.rules(id));
        model.addAttribute("entries", entries);
        model.addAttribute("nominees", nominees);
        model.addAttribute("reports", reports);
        model.addAttribute("tally", categoryService.tally(id));
        model.addAttribute("votes", categoryService.votes(id));
        model.addAttribute("sealed", sealed);
        model.addAttribute("candidates", sealed ? Collections.emptyList() : categoryService.candidates(id));
        model.addAttribute("history", ledgerLines(CategoryAuditLogger.ticketCode(c), 50));
        model.addAttribute("actor", actor(principal));
        return "category/detail";
    }

    @GetMapping("/archive")
    public String archive(Model model, Principal principal) {
        List<AwardCategory> archived = categoryService.archivedCategories();
        Map<Long, Long> votes = new HashMap<>();
        for (AwardCategory c : archived) votes.put(c.getId(), categoryService.votes(c.getId()));
        model.addAttribute("archived", archived);
        model.addAttribute("votes", votes);
        model.addAttribute("now", categoryService.now());
        model.addAttribute("actor", actor(principal));
        return "category/archive";
    }

    @GetMapping("/stats")
    public String stats(Model model, Principal principal) {
        model.addAttribute("stats", statsService.snapshot());
        model.addAttribute("now", categoryService.now());
        model.addAttribute("actor", actor(principal));
        return "category/stats";
    }

    /** Polled every few seconds by category-module.js to keep the numbers live. */
    @GetMapping(value = "/api/live", produces = "application/json")
    @ResponseBody
    public String live() {
        return statsService.snapshot().toJson();
    }

    // ------------------------------------------------------------------ Create

    @GetMapping("/new")
    public String newForm(Model model, Principal principal) {
        model.addAttribute("form", CategoryForm.blank(categoryService.now()));
        model.addAttribute("editing", false);
        model.addAttribute("now", categoryService.now());
        model.addAttribute("actor", actor(principal));
        return "category/form";
    }

    @PostMapping
    public String create(@ModelAttribute CategoryForm form, Model model, Principal principal, RedirectAttributes flash) {
        try {
            AwardCategory c = categoryService.create(form, actor(principal));
            flash.addFlashAttribute(OK, "'" + c.getName() + "' was created with code " + c.getCode() + ".");
            return "redirect:/manager/awards/" + c.getId();
        } catch (CategoryException e) {
            model.addAttribute("form", form);
            model.addAttribute("editing", false);
            model.addAttribute("now", categoryService.now());
            model.addAttribute("actor", actor(principal));
            model.addAttribute(ERROR, e.getMessage());
            return "category/form";
        }
    }

    // ------------------------------------------------------------------ Update

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model, Principal principal, RedirectAttributes flash) {
        try {
            AwardCategory c = categoryService.get(id);
            if (c.isArchived()) throw new CategoryException("'" + c.getName() + "' is archived and read-only. Restore it first.");
            fillEdit(model, c, CategoryForm.of(c), principal);
            return "category/form";
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/manager/awards";
        }
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id, @ModelAttribute CategoryForm form, Model model,
                         Principal principal, RedirectAttributes flash) {
        try {
            List<String> changes = categoryService.update(id, form, actor(principal));
            flash.addFlashAttribute(OK, changes.isEmpty() ? "Nothing was changed."
                    : "Saved. Changed " + String.join("; ", changes) + ".");
            return "redirect:/manager/awards/" + id;
        } catch (CategoryException e) {
            try {
                AwardCategory c = categoryService.get(id);
                fillEdit(model, c, form, principal);
                model.addAttribute(ERROR, e.getMessage());
                return "category/form";
            } catch (CategoryException missing) {
                flash.addFlashAttribute(ERROR, missing.getMessage());
                return "redirect:/manager/awards";
            }
        }
    }

    @PostMapping("/{id}/rules")
    public String addRule(@PathVariable("id") Long id,
                          @RequestParam(value = "ruleType", required = false) String ruleType,
                          @RequestParam(value = "minimum", required = false) String minimum,
                          @RequestParam(value = "text", required = false) String text,
                          Principal principal, RedirectAttributes flash) {
        try {
            EligibilityRule r = categoryService.addRule(id, ruleType, minimum, text, actor(principal));
            flash.addFlashAttribute(OK, "Rule added: " + r.describe() + ".");
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
        }
        return "redirect:/manager/awards/" + id + "#rules";
    }

    @PostMapping("/{id}/rules/{ruleId}/delete")
    public String removeRule(@PathVariable("id") Long id, @PathVariable("ruleId") Long ruleId,
                             Principal principal, RedirectAttributes flash) {
        try {
            categoryService.removeRule(id, ruleId, actor(principal));
            flash.addFlashAttribute(OK, "Rule removed.");
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
        }
        return "redirect:/manager/awards/" + id + "#rules";
    }

    @PostMapping("/{id}/entries")
    public String addEntry(@PathVariable("id") Long id,
                           @RequestParam(value = "nomineeId", required = false) Integer nomineeId,
                           @RequestParam(value = "manualConfirmed", required = false) String manualConfirmed,
                           Principal principal, RedirectAttributes flash) {
        try {
            CategoryEntry e = categoryService.addEntry(id, nomineeId, manualConfirmed != null, actor(principal));
            flash.addFlashAttribute(OK, e.getNomineeName() + " is now on the ballot.");
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
        }
        return "redirect:/manager/awards/" + id + "#ballot";
    }

    @PostMapping("/{id}/entries/{entryId}/delete")
    public String removeEntry(@PathVariable("id") Long id, @PathVariable("entryId") Long entryId,
                              Principal principal, RedirectAttributes flash) {
        try {
            categoryService.removeEntry(id, entryId, actor(principal));
            flash.addFlashAttribute(OK, "Nominee removed from the ballot.");
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
        }
        return "redirect:/manager/awards/" + id + "#ballot";
    }

    // ------------------------------------------------------------------ Delete (archive)

    @PostMapping("/{id}/archive")
    public String archiveCategory(@PathVariable("id") Long id,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  Principal principal, RedirectAttributes flash) {
        try {
            categoryService.archive(id, reason, actor(principal));
            flash.addFlashAttribute(OK, "The category was archived. It no longer appears on ballots or the timeline.");
            return "redirect:/manager/awards/archive";
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/manager/awards/" + id + "#archive";
        }
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable("id") Long id, Principal principal, RedirectAttributes flash) {
        try {
            categoryService.restore(id, actor(principal));
            flash.addFlashAttribute(OK, "The category is back on the timeline.");
            return "redirect:/manager/awards/" + id;
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/manager/awards/archive";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, Principal principal, RedirectAttributes flash) {
        try {
            categoryService.deletePermanently(id, actor(principal));
            flash.addFlashAttribute(OK, "The archived category was deleted permanently.");
        } catch (CategoryException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
        }
        return "redirect:/manager/awards/archive";
    }

    // ------------------------------------------------------------------ helpers

    private void fillEdit(Model model, AwardCategory c, CategoryForm form, Principal principal) {
        LocalDateTime now = categoryService.now();
        model.addAttribute("form", form);
        model.addAttribute("category", c);
        model.addAttribute("phase", c.phaseAt(now));
        model.addAttribute("votes", categoryService.votes(c.getId()));
        model.addAttribute("editing", true);
        model.addAttribute("now", now);
        model.addAttribute("actor", actor(principal));
    }

    /**
     * Module 2's own lines from the Module 4 ledger (newest first): all of them, or only one
     * category's when a ticket code is given.
     */
    private List<AuditTransparencyLog> ledgerLines(String ticketCode, int max) {
        List<AuditTransparencyLog> lines = new ArrayList<>();
        for (AuditTransparencyLog l : ledger.findAllByOrderByTimestampDesc()) {
            if (!CategoryAuditLogger.RECORDED_BY.equals(l.getAuditorUsername())) continue;
            if (ticketCode != null && !ticketCode.equals(l.getTicketCode())) continue;
            lines.add(l);
            if (lines.size() >= max) break;
        }
        return lines;
    }

    /** Who is acting, e.g. "manager (Programme Manager)" - stored with the change and written to the ledger. */
    private String actor(Principal principal) {
        if (principal == null) return "unknown";
        String name = userRepository.findByEmail(principal.getName()).map(User::getUsername).orElse(principal.getName());
        if (name.length() > 90) name = name.substring(0, 90);
        return name + (isAdministrator(principal) ? " (System Administrator)" : " (Programme Manager)");
    }

    private static boolean isAdministrator(Principal principal) {
        if (!(principal instanceof Authentication)) return false;
        for (GrantedAuthority a : ((Authentication) principal).getAuthorities()) {
            if ("ROLE_SYSTEM_ADMINISTRATOR".equals(a.getAuthority())) return true;
        }
        return false;
    }

    /** Things the Programme Manager should look at, most urgent first. */
    private List<BoardNotice> notices(LiveStats stats, LocalDateTime now) {
        List<BoardNotice> warn = new ArrayList<>();
        List<BoardNotice> info = new ArrayList<>();
        for (LiveStats.CategoryLive cl : stats.getCategories()) {
            AwardCategory c = cl.getCategory();
            String link = "/manager/awards/" + c.getId();
            switch (cl.getPhase()) {
                case VOTING_OPEN:
                    if (cl.getEntries() == 0) warn.add(new BoardNotice("warn", "Voting is live in " + c.getName() + " but the ballot is empty.", link + "#ballot"));
                    break;
                case BALLOT_PREP: {
                    long hours = Duration.between(now, c.getVotingStart()).toHours();
                    if (cl.getEntries() == 0) warn.add(new BoardNotice("warn", c.getName() + ": voting opens in " + CategoryHtml.span(now, c.getVotingStart()) + " and no nominee is on the ballot yet.", link + "#ballot"));
                    else if (hours < 48) info.add(new BoardNotice("info", c.getName() + ": voting opens in " + CategoryHtml.span(now, c.getVotingStart()) + " with " + cl.getEntries() + " nominee(s).", link));
                    break;
                }
                case NOMINATIONS_OPEN: {
                    long hours = Duration.between(now, c.getNominationEnd()).toHours();
                    if (hours < 48) info.add(new BoardNotice("info", "Nominations for " + c.getName() + " close in " + CategoryHtml.span(now, c.getNominationEnd()) + ". Draft profiles will then be locked.", link));
                    break;
                }
                case RESULTS:
                    info.add(new BoardNotice("done", c.getName() + ": voting closed with " + cl.getVotes() + " ballot(s). Results are ready for the Selection Committee.", link));
                    break;
                default:
                    break;
            }
        }
        if (stats.getDrafts() > 0 && stats.getActiveCategories() > 0) {
            info.add(new BoardNotice("info", stats.getDrafts() + " nomination(s) are still in Draft and will be locked automatically at their deadline.", null));
        }
        if (stats.getActiveCategories() == 0) {
            warn.add(new BoardNotice("warn", "No active award categories. Create one to open nominations.", "/manager/awards/new"));
        }
        List<BoardNotice> all = new ArrayList<>(warn);
        all.addAll(info);
        return all;
    }
}
