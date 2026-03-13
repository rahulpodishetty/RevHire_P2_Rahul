package com.rev.app.service;

import com.rev.app.dto.SavedJobDto;
import java.util.List;

public interface ISavedJobService {
    SavedJobDto saveJob(Long jobSeekerId, Long jobId);

    void unsaveJob(Long jobSeekerId, Long jobId);

    List<SavedJobDto> getSavedJobsBySeeker(Long jobSeekerId);

    boolean isJobSaved(Long jobSeekerId, Long jobId);
}
