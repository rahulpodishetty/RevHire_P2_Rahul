package com.rev.app.controller;

import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IApplicationService;
import com.rev.app.service.IEmployerService;
import com.rev.app.service.IJobService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller("employerApplicationController")
public class ApplicationController {

    private final IEmployerService employerService;
    private final IApplicationService applicationService;
    private final IJobService jobService;

    public ApplicationController(IEmployerService employerService, IApplicationService applicationService,
            IJobService jobService) {
        this.employerService = employerService;
        this.applicationService = applicationService;
        this.jobService = jobService;
    }

    @GetMapping("/employer/applicants")
    public String listAllApplicants(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "skills", required = false) String skills,
            @RequestParam(name = "experience", required = false) Integer experience,
            @RequestParam(name = "education", required = false) String education,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());

        var appsPage = applicationService.getFilteredApplications(profile.getId(), status, skills, experience,
                education,
                PageRequest.of(page, size));
        var jobs = jobService.getJobsByEmployer(profile.getId(), null, PageRequest.of(0, 100));

        model.addAttribute("applicants", appsPage.getContent());
        model.addAttribute("jobs", jobs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appsPage.getTotalPages());
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "applicants");

        // Pass back params for persistent filtering in UI
        model.addAttribute("status", status);
        model.addAttribute("skills", skills);
        model.addAttribute("experience", experience);
        model.addAttribute("education", education);

        return "employer/applicants";
    }

    @PostMapping("/employer/applicants/bulk-status")
    public String bulkStatusUpdate(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("applicationIds") java.util.List<Long> applicationIds,
            @RequestParam("status") String status) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        applicationService.updateApplicationStatusesBulk(profile.getId(), applicationIds, status);
        return "redirect:/employer/applicants?bulkUpdated=true";
    }

    @GetMapping("/employer/jobs/{jobId}/applicants")
    public String listApplicantsByJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("jobId") Long jobId,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        var appsPage = applicationService.getApplicationsByJob(profile.getId(), jobId, PageRequest.of(page, size));

        var jobs = jobService.getJobsByEmployer(profile.getId(), null, PageRequest.of(0, 100));

        model.addAttribute("applicants", appsPage.getContent());
        model.addAttribute("jobs", jobs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appsPage.getTotalPages());
        model.addAttribute("jobId", jobId);
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "applicants");
        return "employer/applicants";
    }

    @PostMapping("/employer/applicants/{appId}/status")
    public String updateStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("appId") Long appId,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "jobId", required = false) Long jobId) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        applicationService.updateApplicationStatus(profile.getId(), appId, status);

        String redirect = "redirect:/employer/applicants";
        if (jobId != null)
            redirect = "redirect:/employer/jobs/" + jobId + "/applicants";
        return redirect + (redirect.contains("?") ? "&" : "?") + "statusUpdated=true";
    }

    @PostMapping("/employer/applicants/{appId}/notes")
    public String addNote(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("appId") Long appId,
            @RequestParam(name = "note") String note,
            @RequestParam(name = "jobId", required = false) Long jobId) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        applicationService.addNoteToApplication(profile.getId(), appId, note);

        String redirect = "redirect:/employer/applicants";
        if (jobId != null)
            redirect = "redirect:/employer/jobs/" + jobId + "/applicants";
        return redirect + (redirect.contains("?") ? "&" : "?") + "noteAdded=true";
    }
}
