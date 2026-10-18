package com.pation.awardnomination.evaluation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** [INTEGRATION] URLs moved from /admin/shortlist to /evaluation/shortlist; nominees now come from Module 3. */
@Controller
@RequestMapping("/evaluation/shortlist")
public class ShortlistController {

    private final ShortlistService service;
    private final ScoringService scoring;
    public ShortlistController(ShortlistService service, ScoringService scoring) {
        this.service = service;
        this.scoring = scoring;
    }

    @GetMapping   // READ
    public String list(@RequestParam(required = false) String category, Model model) {
        model.addAttribute("entries", service.findAll(category));
        model.addAttribute("category", category);
        model.addAttribute("nominees", service.nomineeOptions());   // [INTEGRATION] Module 3 nominations
        model.addAttribute("categories", scoring.categories());
        return "evaluation/shortlist/list";
    }

    @PostMapping   // CREATE
    public String add(@RequestParam(value = "nomineeId", required = false) Long nomineeId,
                      @RequestParam(value = "categoryName", required = false) String categoryName,
                      RedirectAttributes ra) {
        try {
            List<String> warnings = service.add(nomineeId, categoryName);
            ra.addFlashAttribute("msg", "Nominee added to shortlist");
            if (!warnings.isEmpty()) {
                ra.addFlashAttribute("error", warnings.get(0)
                        + ". Remove the judge from this category on the Judge Panel page.");
            }
        } catch (BusinessRuleException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evaluation/shortlist";
    }

    @PostMapping("/{id}/disqualify")   // UPDATE
    public String disqualify(@PathVariable Long id, @RequestParam String reason, RedirectAttributes ra) {
        try { service.disqualify(id, reason); ra.addFlashAttribute("msg", "Nominee disqualified"); }
        catch (BusinessRuleException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/evaluation/shortlist";
    }

    @PostMapping("/{id}/reinstate")   // UPDATE
    public String reinstate(@PathVariable Long id, RedirectAttributes ra) {
        try { service.reinstate(id); ra.addFlashAttribute("msg", "Nominee reinstated"); }
        catch (BusinessRuleException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/evaluation/shortlist";
    }

    @PostMapping("/{id}/delete")   // DELETE
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("msg", "Entry removed from shortlist");
        return "redirect:/evaluation/shortlist";
    }
}
