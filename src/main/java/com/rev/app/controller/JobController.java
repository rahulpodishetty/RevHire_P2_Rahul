package com.rev.app.controller;

import com.rev.app.dto.JobDto;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IEmployerService;
import com.rev.app.service.IJobService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller("employerJobController")
@RequestMapping("/employer")
public class JobController {

    private final IEmployerService employerService;
    private final IJobService jobService;

    public JobController(IEmployerService employerService, IJobService jobService) {
        this.employerService = employerService;
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public String listJobs(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = employerService.getProfileByUserId(userDetails.getId());
        var jobsPage = jobService.getJobsByEmployer(profile.getId(), status, PageRequest.of(page, size));

        model.addAttribute("jobs", jobsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", jobsPage.getTotalPages());
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("displayIndustry", profile.getCompany().getIndustry());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "jobs");
        return "employer/jobs";
    }

    @GetMapping("/jobs/post")
    public String showPostJobForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("displayIndustry", profile.getCompany().getIndustry());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "post-job");
        model.addAttribute("job", new JobDto());
        return "employer/post_job";
    }

    @PostMapping("/jobs/post")
    public String submitJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("job") JobDto jobDto) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        jobService.createJob(profile.getId(), jobDto);
        return "redirect:/employer/jobs?jobPosted=true";
    }

    @GetMapping("/jobs/{jobId}/edit")
    public String showEditJobForm(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("jobId") Long jobId,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        JobDto job = jobService.getJobById(jobId);

        model.addAttribute("job", job);
        model.addAttribute("displayName", profile.getCompany().getName());
        model.addAttribute("role", "ROLE_EMPLOYER");
        model.addAttribute("activeLink", "jobs");
        return "employer/post_job";
    }

    @PostMapping("/jobs/{jobId}/edit")
    public String updateJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("jobId") Long jobId,
            @ModelAttribute("job") JobDto jobDto) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        jobService.updateJob(profile.getId(), jobId, jobDto);
        return "redirect:/employer/jobs?jobUpdated=true";
    }

    @PostMapping("/jobs/{jobId}/delete")
    public String deleteJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("jobId") Long jobId) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        jobService.deleteJob(profile.getId(), jobId);
        return "redirect:/employer/jobs?jobDeleted=true";
    }

    @PostMapping("/jobs/{jobId}/status")
    public String toggleStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("jobId") Long jobId,
            @RequestParam(name = "status") String status) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = employerService.getProfileByUserId(userDetails.getId());
        JobDto job = jobService.getJobById(jobId);
        job.setStatus(status);
        jobService.updateJob(profile.getId(), jobId, job);
        return "redirect:/employer/jobs?statusUpdated=true";
    }
}
