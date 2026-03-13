package com.rev.app.controller;

import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IJobSeekerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.rev.app.dto.JobSeekerDto;
import com.rev.app.service.IApplicationService;
import com.rev.app.service.IJobService;
import com.rev.app.service.IResumeService;
import com.rev.app.service.ISavedJobService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.rev.app.dto.ResumeDto;

@Controller
@RequestMapping("/seeker")
public class JobSeekerController {

    private final IJobSeekerService jobSeekerService;
    private final IJobService jobService;
    private final IApplicationService applicationService;
    private final IResumeService resumeService;
    private final ISavedJobService savedJobService;

    public JobSeekerController(IJobSeekerService jobSeekerService, IJobService jobService,
            IApplicationService applicationService, IResumeService resumeService, ISavedJobService savedJobService) {
        this.jobSeekerService = jobSeekerService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.resumeService = resumeService;
        this.savedJobService = savedJobService;
    }

    @GetMapping("/dashboard")
    public String seekerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        System.out.println("DEBUG: JobSeekerController.seekerDashboard hit for user: "
                + (userDetails != null ? userDetails.getUsername() : "null"));
        if (userDetails == null)
            return "redirect:/login";

        // EXPLICIT ROLE CHECK
        if (userDetails.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_SEEKER"))) {
            System.err.println("DEBUG: Employer attempted to hit Seeker Dashboard. Redirecting.");
            return "redirect:/login";
        }

        JobSeekerDto profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var stats = jobSeekerService.getDashboardStats(userDetails.getId());

        model.addAttribute("profile", profile);
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "dashboard");
        model.addAllAttributes(stats);

        return "seeker/dashboard";
    }

    @GetMapping("/jobs")
    public String seekerJobs(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "jobType", required = false) String jobType,
            @RequestParam(name = "experience", required = false) Integer experience,
            @RequestParam(name = "company", required = false) String company,
            @RequestParam(name = "salary", required = false) String salary,
            @RequestParam(name = "postedDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate postedDate,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var jobsPage = jobService.getAllJobs(title, location, experience, company, salary, jobType, postedDate,
                PageRequest.of(page, size));

        model.addAttribute("jobs", jobsPage.getContent());
        model.addAttribute("totalJobs", jobsPage.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", jobsPage.getTotalPages());
        model.addAttribute("title", title);
        model.addAttribute("location", location);
        model.addAttribute("jobType", jobType);
        model.addAttribute("experience", experience);
        model.addAttribute("company", company);
        model.addAttribute("salary", salary);
        model.addAttribute("postedDate", postedDate);

        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "search");
        return "seeker/jobs";
    }

    @GetMapping("/jobs/{id}")
    public String seekerJobDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var job = jobService.getJobById(id);
        var resumes = resumeService.getResumesByJobSeeker(profile.getId());

        // Check if already applied to this job
        boolean alreadyApplied = applicationService.hasApplied(profile.getId(), id);

        model.addAttribute("job", job);
        model.addAttribute("hasResumes", !resumes.isEmpty());
        model.addAttribute("alreadyApplied", alreadyApplied);
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "search");
        return "seeker/job_details";
    }

    @PostMapping("/jobs/{id}/apply")
    public String seekerApplyJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @RequestParam(name = "coverLetter", required = false) String coverLetter) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());

        // Check if already applied
        boolean alreadyApplied = applicationService.hasApplied(profile.getId(), id);

        if (alreadyApplied) {
            return "redirect:/seeker/jobs/" + id + "?error=already_applied";
        }

        // Force validation: Check if seeker has a resume
        var resumes = resumeService.getResumesByJobSeeker(profile.getId());
        if (resumes.isEmpty()) {
            return "redirect:/seeker/resumes?error=missing_resume";
        }

        try {
            // Use Active Resume
            ResumeDto activeResume = resumeService.getActiveResume(profile.getId());
            if (activeResume == null) {
                activeResume = resumes.get(0); // Fallback to first if no explicit active
            }
            applicationService.applyToJob(profile.getId(), id, activeResume.getId(),
                    (coverLetter == null || coverLetter.isEmpty()) ? "Applied via Seeker Dashboard." : coverLetter);
            return "redirect:/seeker/applications?applied=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/seeker/jobs/" + id + "?error=already_applied";
        } catch (Exception e) {
            System.err.println("DEBUG: Error applying for job " + id + ": " + e.getMessage());
            return "redirect:/seeker/jobs/" + id + "?error=application_failed";
        }
    }

    @PostMapping("/jobs/{id}/save")
    public String seekerSaveJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        savedJobService.saveJob(profile.getId(), id);

        return "redirect:/seeker/jobs?saved=true";
    }

    @PostMapping("/jobs/{id}/unsave")
    public String seekerUnsaveJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        savedJobService.unsaveJob(profile.getId(), id);

        return "redirect:/seeker/saved?unsaved=true";
    }

    @GetMapping("/applications")
    public String seekerApplications(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "status", required = false) String status,
            Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var applications = applicationService.getApplicationsByJobSeeker(profile.getId(), status,
                PageRequest.of(0, 50));

        model.addAttribute("applications", applications.getContent());
        model.addAttribute("statusFilter", status);
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "applications");
        return "seeker/applications";
    }

    @PostMapping("/applications/{id}/withdraw")
    public String seekerWithdrawApplication(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @RequestParam(name = "reason", required = false) String reason) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        applicationService.withdrawApplication(profile.getId(), id, reason);

        return "redirect:/seeker/applications?withdrawn=true";
    }

    @GetMapping("/resumes")
    public String seekerResumes(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var resumes = resumeService.getResumesByJobSeeker(profile.getId());

        model.addAttribute("resumes", resumes);
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "resumes");
        return "seeker/resumes";
    }

    @GetMapping("/resumes/create")
    public String seekerCreateResumeForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        model.addAttribute("resume", new ResumeDto());
        model.addAttribute("displayName", userDetails.getUsername());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "resumes");
        return "seeker/resume_form";
    }

    @GetMapping("/resumes/{id}/edit")
    public String seekerEditResumeForm(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id, Model model) {
        if (userDetails == null)
            return "redirect:/login";

        ResumeDto resume = resumeService.getResumeById(id);
        model.addAttribute("resume", resume);
        model.addAttribute("displayName", userDetails.getUsername());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "resumes");
        return "seeker/resume_form";
    }

    @PostMapping("/resumes/save")
    public String seekerSaveResume(@AuthenticationPrincipal CustomUserDetails userDetails,
            ResumeDto resumeDto) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        resumeService.saveResume(profile.getId(), resumeDto);

        return "redirect:/seeker/resumes?saved=true";
    }

    @PostMapping("/resumes/{id}/activate")
    public String seekerActivateResume(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        resumeService.setActiveResume(id, profile.getId());

        return "redirect:/seeker/resumes?activated=true";
    }

    @PostMapping("/resumes/{id}/delete")
    public String seekerDeleteResume(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        resumeService.deleteResume(id, profile.getId());

        return "redirect:/seeker/resumes?deleted=true";
    }

    @GetMapping("/improve-profile")
    public String improveProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var resumes = resumeService.getResumesByJobSeeker(profile.getId());

        // Redirection Logic:
        // 1. If profile basics are missing
        if (profile.getPhone() == null || profile.getPhone().isEmpty() || profile.getExperienceYears() == null) {
            return "redirect:/seeker/profile";
        }

        // 2. If no resume at all
        if (resumes.isEmpty()) {
            return "redirect:/seeker/resumes/create";
        }

        // 3. If no file uploaded
        if (resumes.stream().noneMatch(r -> r.getFilePath() != null)) {
            return "redirect:/seeker/resumes";
        }

        // Default
        return "redirect:/seeker/resumes";
    }

    @GetMapping("/saved")
    public String seekerSaved(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        var savedJobs = savedJobService.getSavedJobsBySeeker(profile.getId());

        model.addAttribute("savedJobs", savedJobs);
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "saved");
        return "seeker/saved";
    }

    @GetMapping("/notifications")
    public String seekerNotifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "notifications");
        return "seeker/notifications";
    }

    @GetMapping("/settings")
    public String seekerSettings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        return "redirect:/seeker/profile"; // Simple redirect for now as requested
    }

    @GetMapping("/profile")
    public String seekerProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null)
            return "redirect:/login";
        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        model.addAttribute("profile", profile);
        model.addAttribute("displayName", profile.getName());
        model.addAttribute("role", "ROLE_SEEKER");
        model.addAttribute("activeLink", "profile");
        return "seeker/profile";
    }

    @PostMapping("/profile")
    public String seekerUpdateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "experienceYears", required = false) Integer experienceYears,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "currentEmploymentStatus", required = false) String currentEmploymentStatus,
            @RequestParam(name = "education", required = false) String education,
            @RequestParam(name = "workExperience", required = false) String workExperience,
            @RequestParam(name = "skills", required = false) String skills,
            @RequestParam(name = "certifications", required = false) String certifications,
            @RequestParam(name = "projects", required = false) String projects) {
        if (userDetails == null)
            return "redirect:/login";

        JobSeekerDto dto = new JobSeekerDto();
        dto.setName(name);
        dto.setPhone(phone);
        dto.setExperienceYears(experienceYears);
        dto.setLocation(location);
        dto.setCurrentEmploymentStatus(currentEmploymentStatus);
        dto.setEducation(education);
        dto.setWorkExperience(workExperience);
        dto.setSkills(skills);
        dto.setCertifications(certifications);
        dto.setProjects(projects);

        jobSeekerService.updateProfile(userDetails.getId(), dto);

        return "redirect:/seeker/profile?updated=true";
    }

    @PostMapping("/profile/upload-photo")
    public String seekerUploadProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        if (userDetails == null)
            return "redirect:/login";

        if (file == null || file.isEmpty()) {
            return "redirect:/seeker/profile?error=empty_file";
        }

        try {
            var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
            jobSeekerService.uploadProfileImage(profile.getId(), file);
            return "redirect:/seeker/profile?imageUploaded=true";
        } catch (Exception e) {
            System.err.println("ERROR: Profile image upload failed: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/seeker/profile?error=upload_failed";
        }
    }

    @PostMapping("/resumes/upload")
    public String seekerUploadResume(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "jobId", required = false) Long jobId,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null)
            return "redirect:/login";

        var profile = jobSeekerService.getProfileByUserId(userDetails.getId());
        try {
            resumeService.uploadResume(profile.getId(), file);
            if (jobId != null) {
                return "redirect:/seeker/jobs/" + jobId + "?resume_uploaded=true";
            }
            return "redirect:/seeker/resumes?uploaded=true";
        } catch (Exception e) {
            System.err.println("DEBUG: Resume upload failed: " + e.getMessage());
            if (jobId != null) {
                return "redirect:/seeker/resumes?jobId=" + jobId + "&error=upload_failed";
            }
            return "redirect:/seeker/resumes?error=upload_failed";
        }
    }
}
