package com.pation.awardnomination.evaluation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** [INTEGRATION] URLs moved from /admin/rubrics to /evaluation/rubrics. */
@Controller
@RequestMapping("/evaluation/rubrics")
public class RubricController {

    private final RubricService service;
    private final ScoringService scoring;   // [INTEGRATION] category suggestions
    public RubricController(RubricService service, ScoringService scoring) {
        this.service = service;
        this.scoring = scoring;
    }

    @GetMapping   // READ all
    public String list(Model model) {
        model.addAttribute("rubrics", service.findAll());
        model.addAttribute("categories", scoring.categories());
        return "evaluation/rubric/list";
    }

    @PostMapping   // CREATE
    public String create(@Valid @ModelAttribute Rubric rubric, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/evaluation/rubrics";
        }
        try {
            Rubric saved = service.create(rubric);
            return "redirect:/evaluation/rubrics/" + saved.getId();
        } catch (BusinessRuleException e) {   // [INTEGRATION] one rubric per category
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/evaluation/rubrics";
        }
    }

    @GetMapping("/{id}")   // READ one
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("rubric", service.findById(id));
        return "evaluation/rubric/detail";
    }

    @PostMapping("/{id}")   // UPDATE rubric
    public String update(@PathVariable Long id, @Valid @ModelAttribute Rubric rubric,
                         BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) ra.addFlashAttribute("error", br.getAllErrors().get(0).getDefaultMessage());
        else {
            try { service.update(id, rubric); ra.addFlashAttribute("msg", "Rubric updated"); }
            catch (BusinessRuleException e) { ra.addFlashAttribute("error", e.getMessage()); }
        }
        return "redirect:/evaluation/rubrics/" + id;
    }

    @PostMapping("/{id}/delete")   // DELETE rubric
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("msg", "Rubric deleted");
        return "redirect:/evaluation/rubrics";
    }

    // ---- criteria CRUD ----
    @PostMapping("/{id}/criteria")
    public String addCriterion(@PathVariable Long id, @RequestParam String name,
                               @RequestParam int weight, RedirectAttributes ra) {
        try { service.addCriterion(id, name, weight); ra.addFlashAttribute("msg", "Criterion added"); }
        catch (BusinessRuleException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/evaluation/rubrics/" + id;
    }

    @PostMapping("/{id}/criteria/{cid}")
    public String updateCriterion(@PathVariable Long id, @PathVariable Long cid,
                                  @RequestParam String name, @RequestParam int weight, RedirectAttributes ra) {
        try { service.updateCriterion(id, cid, name, weight); ra.addFlashAttribute("msg", "Criterion updated"); }
        catch (BusinessRuleException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/evaluation/rubrics/" + id;
    }

    @PostMapping("/{id}/criteria/{cid}/delete")
    public String deleteCriterion(@PathVariable Long id, @PathVariable Long cid, RedirectAttributes ra) {
        service.removeCriterion(id, cid);
        ra.addFlashAttribute("msg", "Criterion removed");
        return "redirect:/evaluation/rubrics/" + id;
    }
}
