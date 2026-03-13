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
public class SavedJobDto {
    private Long id;
    private Long jobSeekerId;
    private JobDto job; // Nested DTO to show job details directly
    private LocalDateTime savedOn;
}
