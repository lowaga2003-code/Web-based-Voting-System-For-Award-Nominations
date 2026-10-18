package com.pation.awardnomination.nominee;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/nominee")
public class NomineeController {

    private final NomineeRepository nomineeRepository;
    private final InterviewRepository interviewRepository;

    public NomineeController(NomineeRepository nomineeRepository, InterviewRepository interviewRepository) {
        this.nomineeRepository = nomineeRepository;
        this.interviewRepository = interviewRepository;
    }

    // 1. Home Page
    @GetMapping("/home")
    public String showHomePage(Model model) {
        return "nomineeHome";
    }

    // 2. Login Logic
    @PostMapping("/login")
    public String login(@RequestParam("loginType") String loginType,
                        @RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "password", required = false) String password,
                        @RequestParam(value = "nic", required = false) String nic,
                        HttpSession session, Model model) {

        if ("admin".equals(loginType)) {
            if ("admin".equals(username) && "123".equals(password)) {
                session.setAttribute("role", "ADMIN");

                // =========================================================
                // DESIGN PATTERN USAGE: SINGLETON PATTERN


                SystemLogger.getInstance().logActivity("Admin Logged in to the system.");

                return "redirect:/nominee/list";
            }
        } else if ("user".equals(loginType)) {
            Nominee nominee = nomineeRepository.findFirstByNic(nic);                                  ///////////////////////////////////////////////////////

            if (nominee != null) {
                session.setAttribute("role", "USER");
                session.setAttribute("userNic", nic);

                // =========================================================
                // DESIGN PATTERN USAGE: SINGLETON PATTERN

                SystemLogger.getInstance().logActivity("User (NIC: " + nic + ") Logged in.");

                return "redirect:/nominee/profile";
            }
        }

        model.addAttribute("error", "Invalid login credentials! Please check your NIC.");
        return "nomineeHome";
    }

    // 3. Logout Logic
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/nominee/home";
    }

    // 4. Show Form Page
    @GetMapping("/form")
    public String showForm() {
        return "nomineeForm";
    }

    // 5. Submit Form
    @PostMapping("/submit")
    public String submitNominee(
            @RequestParam("nic") String nic,
            @RequestParam("fullName") String fullName,
            @RequestParam("biography") String biography,
            @RequestParam("companyName") String companyName,
            @RequestParam(value = "evidenceFile", required = false) MultipartFile evidenceFile,
            Model model) {

        try {
            Nominee newNominee = new Nominee();
            newNominee.setNic(nic);
            newNominee.setFullName(fullName);
            newNominee.setBiography(biography);
            newNominee.setCompanyName(companyName);
            newNominee.setStatus("Draft");

            if (evidenceFile != null && !evidenceFile.isEmpty()) {                        /// /////////////////validation//////////////////////////////////////////////
                String contentType = evidenceFile.getContentType();
                if (contentType == null || (!contentType.equals("application/pdf") && !contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
                    throw new NomineeException("Invalid file type! Only PDF, PNG, or JPG allowed.");
                }

                if (evidenceFile.getSize() > 5 * 1024 * 1024) {
                    throw new NomineeException("File size is too large! Maximum allowed is 5MB.");
                }
                newNominee.setEvidenceFileName(evidenceFile.getOriginalFilename());
            }

            nomineeRepository.save(newNominee);
            model.addAttribute("nic", nic);
            return "nomineeSuccess";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "nomineeForm";
        }
    }

    // 6. View Admin List
    @GetMapping("/list")
    public String viewNominees(Model model, HttpSession session) {  //**********READ********
        List<Nominee> nomineeList = nomineeRepository.findAll();

        long totalCount = nomineeList.size();
        long approvedCount = nomineeList.stream().filter(n -> "Approved".equals(n.getStatus())).count();
        long reviewCount = nomineeList.stream().filter(n -> "Under Review".equals(n.getStatus())).count();
        long draftCount = nomineeList.stream().filter(n -> "Draft".equals(n.getStatus())).count();

        model.addAttribute("nominees", nomineeList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("draftCount", draftCount);

        return "nomineeList";
    }

    // 7. View User Single Profile
    @GetMapping("/profile")
    public String viewNomineeProfile(HttpSession session, Model model) {
        String nic = (String) session.getAttribute("userNic");
        if (nic == null) {
            return "redirect:/nominee/home";
        }
        Nominee nominee = nomineeRepository.findFirstByNic(nic);
        model.addAttribute("nominee", nominee);
        return "nomineeProfile";
    }

    // 8. Show Edit Form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Nominee nominee = nomineeRepository.findById(id).orElse(null);
        model.addAttribute("nominee", nominee);
        return "nomineeEdit";
    }

    // 9. Update Nominee Details
    @PostMapping("/update/{id}")
    public String updateNominee(@PathVariable("id") Integer id,                   //*********UPDATE*********
                                @RequestParam("fullName") String fullName,
                                @RequestParam("biography") String biography,
                                @RequestParam("companyName") String companyName,
                                @RequestParam(value = "status", required = false) String status,
                                HttpSession session) {

        Nominee nominee = nomineeRepository.findById(id).orElse(null);
        String role = (String) session.getAttribute("role");

        if (nominee != null) {
            nominee.setFullName(fullName);
            nominee.setBiography(biography);
            nominee.setCompanyName(companyName);

            if ("ADMIN".equals(role) && status != null) {
                nominee.setStatus(status);
            } else if ("USER".equals(role)) {
                nominee.setStatus("Under Review");
            }

            nomineeRepository.save(nominee);
        }

        if ("ADMIN".equals(role)) {
            return "redirect:/nominee/list";
        } else {
            return "redirect:/nominee/profile";
        }
    }

    // 10. Delete Nominee
    @GetMapping("/delete/{id}")
    public String deleteNominee(@PathVariable("id") Integer id) {  //**********DELEATE**********
        nomineeRepository.deleteById(id);
        return "redirect:/nominee/list";
    }

    // 11. Interview Page එක පෙන්වීම
    @GetMapping("/interviews")
    public String viewInterviews(Model model, HttpSession session) {
        String role = (String) session.getAttribute("role");
        String userNic = (String) session.getAttribute("userNic");

        model.addAttribute("interviews", interviewRepository.findAll());

        if ("USER".equals(role) && userNic != null) {
            Interview bookedInterview = interviewRepository.findByNomineeNic(userNic);
            model.addAttribute("bookedInterview", bookedInterview);
        }

        return "interviewSchedule";
    }

    // 12. Admin අලුතින් Interview Slot එකක් දැමීම
    @PostMapping("/interview/add")
    public String addInterviewSlot(@RequestParam("interviewDate") String date,
                                   @RequestParam("interviewTime") String time,
                                   @RequestParam("location") String location,
                                   HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role)) {
            Interview slot = new Interview();
            slot.setInterviewDate(date);
            slot.setInterviewTime(time);
            slot.setLocation(location);
            slot.setStatus("Available");
            interviewRepository.save(slot);
        }
        return "redirect:/nominee/interviews";
    }

    // 13. Nominee කෙනෙක් Slot එකක් Book කරගැනීම
    @GetMapping("/interview/book/{id}")
    public String bookInterviewSlot(@PathVariable("id") Integer id, HttpSession session) {
        String role = (String) session.getAttribute("role");
        String userNic = (String) session.getAttribute("userNic");

        if ("USER".equals(role) && userNic != null) {
            Interview existing = interviewRepository.findByNomineeNic(userNic);
            if (existing != null) {
                existing.setStatus("Available");
                existing.setNomineeNic(null);
                interviewRepository.save(existing);
            }

            Interview slot = interviewRepository.findById(id).orElse(null);
            if (slot != null && "Available".equals(slot.getStatus())) {
                slot.setStatus("Booked");
                slot.setNomineeNic(userNic);
                interviewRepository.save(slot);

                // =========================================================
                // DESIGN PATTERN USAGE: SINGLETON PATTERN

                SystemLogger.getInstance().logActivity("Interview Slot " + id + " booked by Nominee NIC: " + userNic);
            }
        }
        return "redirect:/nominee/interviews";
    }
}