package com.rev.app.mapper;

import com.rev.app.dto.ApplicationNoteDto;
import com.rev.app.entity.ApplicationNote;
import org.springframework.stereotype.Component;

@Component
public class ApplicationNoteMapper {
    public ApplicationNoteDto toDto(ApplicationNote note) {
        if (note == null)
            return null;
        return ApplicationNoteDto.builder()
                .id(note.getId())
                .applicationId(note.getApplication() != null ? note.getApplication().getId() : null)
                .employerId(note.getEmployer() != null ? note.getEmployer().getId() : null)
                .noteText(note.getNoteText())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
