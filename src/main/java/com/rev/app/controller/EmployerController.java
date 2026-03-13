package com.rev.app.controller;

import com.rev.app.dto.EmployerDto;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IEmployerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final IEmployerService employerService;

    public EmployerController(IEmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping("/company")
    public String employerCompany(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = employerService.getProfileByUserId(userDetails.getId());
        model.addAttribute("profile", profile.getCompany());
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "company");
        return "employer/company";
    }

    @GetMapping("/profile")
    public String employerProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "profile");
        return "employer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("profile") EmployerDto dto) {
        if (userDetails == null)
            return "redirect:/login";
        employerService.updateProfile(userDetails.getId(), dto);
        return "redirect:/employer/profile?updated=true";
    }

    @GetMapping("/notifications")
    public String employerNotifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "notifications");
        return "employer/notifications";
    }

    @PostMapping("/company/update")
    public String updateCompany(@AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("profile") com.rev.app.dto.CompanyDto companyDto) {
        if (userDetails == null)
            return "redirect:/login";

        EmployerDto employerDto = new EmployerDto();
        employerDto.setCompany(companyDto);
        employerService.updateProfile(userDetails.getId(), employerDto);
        return "redirect:/employer/company?updated=true";
    }

    @PostMapping("/company/logo")
    public String employerUploadCompanyLogo(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = employerService.getProfileByUserId(userDetails.getId());
        employerService.uploadCompanyLogo(profile.getId(), file);

        return "redirect:/employer/company?imageUploaded=true";
    }

    @GetMapping("/settings")
    public String employerSettings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        return "redirect:/employer/profile";
    }
}
