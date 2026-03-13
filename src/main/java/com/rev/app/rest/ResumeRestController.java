package com.rev.app.rest;

import com.rev.app.dto.ResumeDto;
import com.rev.app.service.IResumeService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/resumes")
public class ResumeRestController {

    private final IResumeService resumeService;

    public ResumeRestController(IResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadResume(@PathVariable("id") Long id) {
        ResumeDto resume = resumeService.getResumeById(id);

        if (resume.getFilePath() != null) {
            try {
                Path path = Paths.get(resume.getFilePath());
                Resource resource = new UrlResource(path.toUri());

                if (resource.exists()) {
                    String contentType = "application/octet-stream";
                    if (resume.getFileName().endsWith(".pdf"))
                        contentType = "application/pdf";
                    else if (resume.getFileName().endsWith(".docx"))
                        contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "inline; filename=\"" + resume.getFileName() + "\"")
                            .body(resource);
                }
            } catch (MalformedURLException e) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDto> getResumeData(@PathVariable("id") Long id) {
        return ResponseEntity.ok(resumeService.getResumeById(id));
    }
}
