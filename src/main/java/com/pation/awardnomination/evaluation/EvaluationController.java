package com.pation.awardnomination.evaluation;

import com.pation.awardnomination.nominee.Nominee;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [INTEGRATION] Module 5 home (was HomeController with /admin and / - those clashed with Module 1 and the
 * main dashboard) + the new candidate, scoring, results and winner pages.
 *
 *  GET  /evaluation                              home: the Chairperson's steps and live counts
 *  GET  /evaluation/candidates/{id}              Module 3 evidence + interview + judges' marks
 *  POST /evaluation/candidates/{id}/scores       save / re-evaluate one judge's marks
 *  GET  /evaluation/results?category=            ranking per category
 *  POST /evaluation/candidates/{id}/winner       mark as Winner (one per category)
 *  POST /evaluation/candidates/{id}/revoke-winner
 */
@Controller
@RequestMapping("/evaluation")
public class EvaluationController {

    private final ScoringService scoring;
    private final ShortlistService shortlist;

    public EvaluationController(ScoringService scoring, ShortlistService shortlist) {
        this.scoring = scoring;
        this.shortlist = shortlist;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("overview", scoring.overview());
        return "evaluation/index";
    }

    @GetMapping("/candidates/{id}")
    public String candidate(@PathVariable Long id, Model model) {
        ShortlistEntry entry = shortlist.findById(id);
        List<ScoreSheet> sheets = scoring.sheets(entry);
        Nominee nominee = scoring.nominee(entry).orElse(null);
        model.addAttribute("entry", entry);
        model.addAttribute("nominee", nominee);
        model.addAttribute("interview", scoring.interview(nominee));
        model.addAttribute("rubric", scoring.rubricFor(entry).orElse(null));
        model.addAttribute("sheets", sheets);
        model.addAttribute("average", ScoringService.average(sheets));
        return "evaluation/candidate";
    }

    @PostMapping("/candidates/{id}/scores")
    public String saveScores(@PathVariable Long id, @RequestParam(value = "judgeId", required = false) Long judgeId,
                             HttpServletRequest request, RedirectAttributes ra) {
        try {
            if (judgeId == null) throw new BusinessRuleException("Choose the judge whose marks you are entering.");
            Map<Long, Integer> marks = new HashMap<>();
            for (Map.Entry<String, String[]> p : request.getParameterMap().entrySet()) {
                if (p.getKey().startsWith("score_") && p.getValue().length > 0 && !p.getValue()[0].isBlank()) {
                    try {
                        marks.put(Long.valueOf(p.getKey().substring(6)), Integer.valueOf(p.getValue()[0].trim()));
                    } catch (NumberFormatException e) {
                        throw new BusinessRuleException("Marks must be whole numbers between 0 and " + EvaluationScore.MAX_SCORE + ".");
                    }
                }
            }
            boolean reEvaluated = scoring.saveScores(id, judgeId, marks);
            ra.addFlashAttribute("msg", reEvaluated ? "Marks re-evaluated" : "Marks saved");
        } catch (BusinessRuleException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/evaluation/candidates/" + id;
    }

    @GetMapping("/results")
    public String results(@RequestParam(value = "category", required = false) String category, Model model) {
        List<String> categories = scoring.categories();
        String chosen = (category == null || category.isBlank()) ? (categories.isEmpty() ? null : categories.get(0)) : category.trim();
        model.addAttribute("categories", categories);
        model.addAttribute("category", chosen);
        if (chosen != null) {
            model.addAttribute("results", scoring.results(chosen));
            model.addAttribute("panel", scoring.panel(chosen));
            // [INTEGRATION v5] Module 2 timeline + public vote for this category
            model.addAttribute("awardCategory", scoring.awardCategory(chosen).orElse(null));
            model.addAttribute("votingPhase", scoring.votingPhase(chosen));
        }
        return "evaluation/results";
    }

    @PostMapping("/candidates/{id}/winner")
    public String winner(@PathVariable Long id, RedirectAttributes ra) {
        ShortlistEntry e = shortlist.findById(id);
        try {
            scoring.markWinner(id);
            ra.addFlashAttribute("msg", e.getNomineeName() + " is the winner of " + e.getCategoryName());
        } catch (BusinessRuleException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        ra.addAttribute("category", e.getCategoryName());
        return "redirect:/evaluation/results";
    }

    @PostMapping("/candidates/{id}/revoke-winner")
    public String revokeWinner(@PathVariable Long id, RedirectAttributes ra) {
        ShortlistEntry e = shortlist.findById(id);
        try {
            scoring.revokeWinner(id);
            ra.addFlashAttribute("msg", "Winner status removed");
        } catch (BusinessRuleException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        ra.addAttribute("category", e.getCategoryName());
        return "redirect:/evaluation/results";
    }
}
