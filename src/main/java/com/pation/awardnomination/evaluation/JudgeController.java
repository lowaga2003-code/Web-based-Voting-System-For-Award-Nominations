package com.pation.awardnomination.evaluation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * [INTEGRATION] URLs moved from /admin/** (System Administrator only, Module 1) to /evaluation/**
 * (Selection Committee Chairperson). Views moved to /WEB-INF/jsp/evaluation/judge/.
 */
@Controller
@RequestMapping("/evaluation")
public class JudgeController {

    private final JudgeService service;
    private final ScoringService scoring;   // [INTEGRATION] category list for the panel form
    public JudgeController(JudgeService service, ScoringService scoring) {
        this.service = service;
        this.scoring = scoring;
    }

    // ---------- READ ----------
    @GetMapping("/judges")
    public String list(Model model) {
        model.addAttribute("judges", service.findAll());
        return "evaluation/judge/list";
    }

    // ---------- CREATE ----------
    @GetMapping("/judges/new")
    public String newForm(Model model) {
        model.addAttribute("judge", new Judge());
        model.addAttribute("action", "/evaluation/judges");
        return "evaluation/judge/form";
    }

    @PostMapping("/judges")
    public String create(@Valid @ModelAttribute("judge") Judge judge, BindingResult br,
                         Model model, RedirectAttributes ra) {
        model.addAttribute("action", "/evaluation/judges");
        if (br.hasErrors()) return formWithErrors(br, model);
        try {
            service.create(judge);
        } catch (BusinessRuleException e) {
            model.addAttribute("error", e.getMessage());
            return "evaluation/judge/form";
        }
        ra.addFlashAttribute("msg", "Judge added");
        return "redirect:/evaluation/judges";
    }

    // ---------- UPDATE ----------
    @GetMapping("/judges/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("judge", service.findById(id));
        model.addAttribute("action", "/evaluation/judges/" + id);
        return "evaluation/judge/form";
    }

    @PostMapping("/judges/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("judge") Judge judge,
                         BindingResult br, Model model, RedirectAttributes ra) {
        model.addAttribute("action", "/evaluation/judges/" + id);
        if (br.hasErrors()) return formWithErrors(br, model);
        try {
            service.update(id, judge);
        } catch (BusinessRuleException e) {
            model.addAttribute("error", e.getMessage());
            return "evaluation/judge/form";
        }
        ra.addFlashAttribute("msg", "Judge updated");
        return "redirect:/evaluation/judges";
    }

    // ---------- DELETE ----------
    @PostMapping("/judges/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("msg", "Judge deleted");
        return "redirect:/evaluation/judges";
    }

    // ---------- Judge panel (assignments) ----------
    @GetMapping("/assignments")
    public String assignments(Model model) {
        model.addAttribute("judges", service.findAll());
        model.addAttribute("assignments", service.findAllAssignments());
        model.addAttribute("conflicts", service.findConflicts());   // [INTEGRATION]
        model.addAttribute("categories", scoring.categories());     // [INTEGRATION]
        return "evaluation/judge/assignments";
    }

    @PostMapping("/assignments")
    public String assign(@RequestParam Long judgeId, @RequestParam String categoryName, RedirectAttributes ra) {
        try {
            service.assign(judgeId, categoryName);
            ra.addFlashAttribute("msg", "Judge assigned to category");
        } catch (BusinessRuleException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evaluation/assignments";
    }

    @PostMapping("/assignments/{id}/delete")
    public String removeAssignment(@PathVariable Long id,
                                   @RequestParam(value = "conflict", required = false) String conflict,
                                   RedirectAttributes ra) {
        boolean becauseOfConflict = "yes".equals(conflict);
        service.removeAssignment(id, becauseOfConflict);
        ra.addFlashAttribute("msg", becauseOfConflict ? "Judge removed from the panel because of the conflict of interest"
                : "Judge removed from panel");
        return "redirect:/evaluation/assignments";
    }

    /** [INTEGRATION] the pages use scriptlets (no JSTL / Spring form tags), so field errors go in a simple map. */
    private static String formWithErrors(BindingResult br, Model model) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : br.getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        model.addAttribute("fieldErrors", errors);
        return "evaluation/judge/form";
    }
}
