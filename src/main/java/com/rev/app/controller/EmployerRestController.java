package com.rev.app.controller;

import com.rev.app.dto.ApplicationDto;
import com.rev.app.dto.EmployerDto;
import com.rev.app.dto.JobDto;
import com.rev.app.security.CustomUserDetails;
import com.rev.app.service.IApplicationService;
import com.rev.app.service.IEmployerService;
import com.rev.app.service.IJobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employer")
@PreAuthorize("hasRole('EMPLOYER')")
public class EmployerRestController {

    private final IEmployerService IEmployerService;
    private final IJobService IJobService;
    private final IApplicationService IApplicationService;

    public EmployerRestController(IEmployerService IEmployerService, IJobService IJobService,
            IApplicationService IApplicationService) {
        this.IEmployerService = IEmployerService;
        this.IJobService = IJobService;
        this.IApplicationService = IApplicationService;
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployerDto> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(IEmployerService.getProfileByUserId(userDetails.getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<EmployerDto> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody EmployerDto dto) {
        return ResponseEntity.ok(IEmployerService.updateProfile(userDetails.getId(), dto));
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobDto> postJob(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody JobDto dto) {
        EmployerDto profile = IEmployerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity.ok(IJobService.createJob(profile.getId(), dto));
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobDto>> getMyJobs(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        EmployerDto profile = IEmployerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity.ok(IJobService.getJobsByEmployer(profile.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<Page<ApplicationDto>> getJobApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        EmployerDto profile = IEmployerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity
                .ok(IApplicationService.getApplicationsByJob(profile.getId(), jobId, PageRequest.of(page, size)));
    }

    @PutMapping("/applications/{applicationId}/status")
    public ResponseEntity<ApplicationDto> updateApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long applicationId,
            @RequestParam String status) {
        EmployerDto profile = IEmployerService.getProfileByUserId(userDetails.getId());
        return ResponseEntity.ok(IApplicationService.updateApplicationStatus(profile.getId(), applicationId, status));
    }
}

