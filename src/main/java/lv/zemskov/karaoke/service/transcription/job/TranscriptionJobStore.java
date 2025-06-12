package lv.zemskov.karaoke.service.transcription.job;

import lv.zemskov.karaoke.service.job.JobStore;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface TranscriptionJobStore extends JobStore<UUID> {
    List<UUID> findStuckJobs(Duration timeout);
    void cleanupOldJobs(Duration retentionPeriod);
}