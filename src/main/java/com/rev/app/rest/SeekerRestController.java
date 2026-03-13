package com.rev.app.rest;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.dto.JobSeekerDto;
import com.rev.app.dto.ResumeDto;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IApplicationService;
import com.rev.app.service.IJobSeekerService;
import com.rev.app.service.IResumeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seeker")
@PreAuthorize("hasRole('SEEKER')")
public class SeekerRestController {

    private final IJobSeekerService IJobSeekerService;
    private final IResumeService IResumeService;
    private final IApplicationService IApplicationService;

    public SeekerRestController(IJobSeekerService IJobSeekerService, IResumeService IResumeService,
            IApplicationService IApplicationService) {
        this.IJobSeekerService = IJobSeekerService;
        this.IResumeService = IResumeService;
        this.IApplicationService = IApplicationService;
    }

    @GetMapping("/profile")
    public ResponseEntity<JobSeekerDto> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(IJobSeekerService.getProfileByUserId(userDetails.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<JobSeekerDto> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody JobSeekerDto dto) {
        return ResponseEntity.ok(IJobSeekerService.updateProfile(userDetails.getId(), dto));
    }

    @PostMapping("/resumes")
    public ResponseEntity<ResumeDto> addResume(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ResumeDto dto) {
        JobSeekerDto profile = IJobSeekerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity.ok(IResumeService.saveResume(profile.getId(), dto));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApplicationDto> applyForJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "jobId") Long jobId,
            @RequestParam(name = "resumeId") Long resumeId,
            @RequestParam(name = "coverLetter", required = false) String coverLetter) {
        JobSeekerDto profile = IJobSeekerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity.ok(IApplicationService.applyToJob(profile.getId(), jobId, resumeId, coverLetter));
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationDto>> getApplications(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        JobSeekerDto profile = IJobSeekerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity
                .ok(IApplicationService.getApplicationsByJobSeeker(profile.getId(), null, PageRequest.of(page, size)));
    }
}
