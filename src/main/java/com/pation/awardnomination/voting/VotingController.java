package com.pation.awardnomination.voting;

import com.pation.awardnomination.admin.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Voting pages for the Registered User (Voter). Everything is under /voter/ballot:
 *  - SecurityConfig allows /voter/** only for ROLE_VOTER (after the Module 1 OTP step)
 *  - Module 6's policy gate asks for the VOTING consent before any /voter page opens
 */
@Controller
@RequestMapping("/voter/ballot")
public class VotingController {

    private static final String OK = "vtOk";
    private static final String ERROR = "vtError";

    private final VotingService votingService;

    public VotingController(VotingService votingService) {
        this.votingService = votingService;
    }

    @GetMapping
    public String home(Principal principal, Model model) {
        try {
            User voter = votingService.voter(name(principal));
            model.addAttribute("voter", voter);
            model.addAttribute("cards", votingService.cards(voter));
        } catch (VotingException e) {
            model.addAttribute(ERROR, e.getMessage());
        }
        model.addAttribute("now", votingService.now());
        return "voting/ballot";
    }

    @GetMapping("/{categoryId}")
    public String category(@PathVariable("categoryId") Long categoryId, Principal principal, Model model,
                           RedirectAttributes flash) {
        try {
            User voter = votingService.voter(name(principal));
            model.addAttribute("voter", voter);
            model.addAttribute("ballot", votingService.page(voter, categoryId));
            model.addAttribute("now", votingService.now());
            return "voting/category";
        } catch (VotingException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/voter/ballot";
        }
    }

    @PostMapping("/{categoryId}")
    public String cast(@PathVariable("categoryId") Long categoryId,
                       @RequestParam(value = "nomineeId", required = false) Integer nomineeId,
                       @RequestParam(value = "confirm", required = false) String confirm,
                       Principal principal, HttpServletRequest request, RedirectAttributes flash) {
        try {
            User voter = votingService.voter(name(principal));
            Vote vote = votingService.cast(voter, categoryId, nomineeId, confirm != null,
                    request.getRemoteAddr(), request.getHeader("User-Agent"));
            flash.addFlashAttribute(OK, "Your vote is sealed. Keep your receipt code " + vote.getReceiptCode() + ".");
            flash.addFlashAttribute("vtFresh", Boolean.TRUE);
            return "redirect:/voter/ballot/receipt/" + vote.getReceiptCode();
        } catch (DuplicateVoteException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            if (e.getExistingReceipt() != null) return "redirect:/voter/ballot/receipt/" + e.getExistingReceipt();
            return "redirect:/voter/ballot/" + categoryId;
        } catch (VotingException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/voter/ballot/" + categoryId;
        }
    }

    @GetMapping("/receipt/{code}")
    public String receipt(@PathVariable("code") String code, Principal principal, Model model, RedirectAttributes flash) {
        try {
            User voter = votingService.voter(name(principal));
            model.addAttribute("voter", voter);
            model.addAttribute("receipt", votingService.receipt(voter, code));
            model.addAttribute("now", votingService.now());
            return "voting/receipt";
        } catch (VotingException e) {
            flash.addFlashAttribute(ERROR, e.getMessage());
            return "redirect:/voter/ballot/history";
        }
    }

    @GetMapping("/history")
    public String history(Principal principal, Model model) {
        try {
            User voter = votingService.voter(name(principal));
            model.addAttribute("voter", voter);
            model.addAttribute("receipts", votingService.history(voter));
        } catch (VotingException e) {
            model.addAttribute(ERROR, e.getMessage());
        }
        model.addAttribute("now", votingService.now());
        return "voting/history";
    }

    @GetMapping("/results")
    public String results(Principal principal, Model model) {
        try {
            model.addAttribute("voter", votingService.voter(name(principal)));
        } catch (VotingException e) {
            model.addAttribute(ERROR, e.getMessage());
        }
        model.addAttribute("results", votingService.results());
        model.addAttribute("now", votingService.now());
        return "voting/results";
    }

    private static String name(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
