package com.rev.app.controller;

import com.rev.app.dto.EmployerDto;
import com.rev.app.exception.ResourceNotFoundException;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IEmployerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller("employerDashboardController")
@RequestMapping("/employer/dashboard")
public class DashboardController {

    private final IEmployerService employerService;

    public DashboardController(IEmployerService employerService) {
        this.employerService = employerService;
    }

    @GetMapping
    public String employerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        // Role check
        if (userDetails.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"))) {
            return "redirect:/login?error=true";
        }

        EmployerDto profile;
        try {
            profile = employerService.getProfileByUserId(userDetails.getId());
        } catch (ResourceNotFoundException e) {
            return "redirect:/employer/profile?setup=true";
        }

        var stats = employerService.getDashboardStats(userDetails.getId());

        String displayName = (profile.getCompany() != null) ? profile.getCompany().getName() : "Company Name";
        String displayIndustry = (profile.getCompany() != null) ? profile.getCompany().getIndustry() : "Industry";

        model.addAttribute("profile", profile);
        model.addAttribute("displayName", displayName);
        model.addAttribute("displayIndustry", displayIndustry);
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "dashboard");
        model.addAllAttributes((Map<String, ?>) stats);

        return "employer/dashboard";
    }
}
