package com.pation.awardnomination.category;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Module 2 - the public side of the time engine, open to every signed-in user:
 * the award calendar, and the notice shown when a Module 3 action is locked by a deadline.
 */
@Controller
public class TimelineController {

    private final CategoryService categoryService;

    public TimelineController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/timeline")
    public String timeline(Model model, HttpServletRequest request) {
        model.addAttribute("categories", categoryService.activeCategories());
        model.addAttribute("now", categoryService.now());
        model.addAttribute("isManager", isStaff(request));
        model.addAttribute("isVoter", request.isUserInRole("VOTER"));
        return "category/timeline";
    }

    @GetMapping("/timeline/locked")
    public String locked(@RequestParam(value = "reason", required = false) String reason,
                         @RequestParam(value = "nominee", required = false) Integer nominee,
                         Model model, HttpServletRequest request) {
        NominationLocks locks = categoryService.locks();
        model.addAttribute("reason", "closed".equals(reason) ? "closed" : "locked");
        model.addAttribute("deadline", locks.deadlineFor(nominee));
        model.addAttribute("categories", categoryService.activeCategories());
        model.addAttribute("now", categoryService.now());
        model.addAttribute("isManager", isStaff(request));
        return "category/locked";
    }

    /** The Programme Manager and the System Administrator also get the Module 2 management menu. */
    private static boolean isStaff(HttpServletRequest request) {
        return request.isUserInRole("PROGRAM_MANAGER") || request.isUserInRole("SYSTEM_ADMINISTRATOR");
    }
}
