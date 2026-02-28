package com.rev.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationNoteDto {
    private Long id;
    private Long applicationId;
    private Long employerId;
    private String noteText;
    private LocalDateTime createdAt;
}
