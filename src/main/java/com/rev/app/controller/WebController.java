package com.rev.app.controller;

import com.rev.app.dto.JwtResponseDto;
import com.rev.app.dto.LoginDto;
import com.rev.app.dto.RegisterEmployerDto;
import com.rev.app.dto.RegisterJobSeekerDto;
import com.rev.app.service.IAuthService;
import com.rev.app.service.IEmployerService;
import com.rev.app.service.IJobSeekerService;
import com.rev.app.service.IJobService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.rev.app.security.CustomUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    private final IAuthService IAuthService;
    private final IJobService IJobService;
    private final IJobSeekerService IJobSeekerService;
    private final IEmployerService IEmployerService;

    public WebController(IAuthService IAuthService, IJobService IJobService,
            IJobSeekerService IJobSeekerService, IEmployerService IEmployerService) {
        this.IAuthService = IAuthService;
        this.IJobService = IJobService;
        this.IJobSeekerService = IJobSeekerService;
        this.IEmployerService = IEmployerService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/auth/login")
    public String handleLogin(@ModelAttribute LoginDto loginDto, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        try {
            JwtResponseDto jwtResponse = IAuthService.login(loginDto);

            // Set JWT as cookie
            Cookie cookie = new Cookie("jwtToken", jwtResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            if ("ROLE_JOB_SEEKER".equals(jwtResponse.getRole())) {
                return "redirect:/seeker/dashboard";
            } else if ("ROLE_EMPLOYER".equals(jwtResponse.getRole())) {
                return "redirect:/employer/dashboard";
            }
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/login";
        }
    }

    @GetMapping("/auth/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    @GetMapping("/register/seeker")
    public String registerSeekerPage() {
        return "register_seeker";
    }

    @PostMapping("/auth/register/seeker")
    public String registerSeeker(@ModelAttribute RegisterJobSeekerDto dto, RedirectAttributes redirectAttributes) {
        try {
            IAuthService.registerJobSeeker(dto);
            redirectAttributes.addFlashAttribute("success", "Registration successful. Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/register/seeker";
        }
    }

    @GetMapping("/register/employer")
    public String registerEmployerPage() {
        return "register_employer";
    }

    @PostMapping("/auth/register/employer")
    public String registerEmployer(@ModelAttribute RegisterEmployerDto dto, RedirectAttributes redirectAttributes) {
        try {
            IAuthService.registerEmployer(dto);
            redirectAttributes.addFlashAttribute("success", "Registration successful. Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/register/employer";
        }
    }

    @GetMapping("/seeker/dashboard")
    public String seekerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = IJobSeekerService.getProfileByUserId(userDetails.getId());
        var stats = IJobSeekerService.getDashboardStats(userDetails.getId());

        model.addAttribute("profile", profile);
        model.addAttribute("role", "ROLE_JOB_SEEKER");
        model.addAttribute("activeLink", "dashboard");
        model.addAllAttributes(stats);

        return "seeker_dashboard";
    }

    @GetMapping("/employer/dashboard")
    public String employerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = IEmployerService.getProfileByUserId(userDetails.getId());
        var stats = IEmployerService.getDashboardStats(userDetails.getId());

        model.addAttribute("profile", profile);
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "dashboard");
        model.addAllAttributes(stats);

        return "employer_dashboard";
    }

}
