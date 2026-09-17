package com.pation.awardnomination.nominee;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List; // List collection එක පාවිච්චි කරන්න මේක අනිවාර්යයෙන් ඕනේ

@Controller
@RequestMapping("/nominee")
public class NomineeController {

    // ABSTRACTION: We use the Repository Interface here. We don't need to know how SQL insert works.
    private final NomineeRepository nomineeRepository;

    // Constructor Injection (Good Software Engineering Practice)
    public NomineeController(NomineeRepository nomineeRepository) {
        this.nomineeRepository = nomineeRepository;
    }

    @GetMapping("/form")
    public String showForm() {
        return "nomineeForm";
    }

    @PostMapping("/submit")
    public String submitNominee(
            @RequestParam("fullName") String fullName,
            @RequestParam("biography") String biography,
            @RequestParam("companyName") String companyName,
            @RequestParam("evidenceFile") MultipartFile evidenceFile,
            Model model) {

        try {
            String contentType = evidenceFile.getContentType();
            if (contentType == null || (!contentType.equals("application/pdf") && !contentType.equals("image/png"))) {
                throw new NomineeException("Invalid file type! Only PDF or PNG allowed.");
            }

            if (evidenceFile.getSize() > 5 * 1024 * 1024) {
                throw new NomineeException("File size is too large! Maximum allowed is 5MB.");
            }

            // ENCAPSULATION: Creating a new object and setting data safely using setters
            Nominee newNominee = new Nominee();
            newNominee.setFullName(fullName);
            newNominee.setBiography(biography);
            newNominee.setCompanyName(companyName);
            newNominee.setEvidenceFileName(evidenceFile.getOriginalFilename());

            // Saving to the Database using the Repository
            nomineeRepository.save(newNominee);

            model.addAttribute("successMessage", "Profile and Evidence submitted successfully!");
            return "nomineeForm";

        } catch (NomineeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "nomineeForm";
        }
    }

    // --- මෙන්න අලුතින් එකතු කරපු READ (View) කොටස ---
    @GetMapping("/list")
    public String viewNominees(Model model) {

        // DATA STRUCTURE & ABSTRACTION: Using Java Collections (List) to hold data.
        List<Nominee> nomineeList = nomineeRepository.findAll();

        // මේ List එක JSP එකට යවන්න model එකට දානවා
        model.addAttribute("nominees", nomineeList);

        return "nomineeList"; // මේකෙන් nomineeList.jsp file එක load කරනවා
    }
}