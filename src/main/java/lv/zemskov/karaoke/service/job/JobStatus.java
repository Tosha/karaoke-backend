package lv.zemskov.karaoke.service.job;

import java.time.Instant;

public record JobStatus<T>(
        T jobId,
        JobState state,
        String message,
        Object result,
        String error,
        Instant createdAt,
        Instant updatedAt
) {}