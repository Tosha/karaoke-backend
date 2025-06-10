package lv.zemskov.karaoke.service.transcription.job;

import lv.zemskov.karaoke.model.TranscriptionResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TranscriptionJobStore {

    private final Map<UUID, TranscriptionJobStatus> jobStatusMap = new ConcurrentHashMap<>();

    public void setInProgress(UUID jobId) {
        jobStatusMap.put(jobId, new TranscriptionJobStatus(TranscriptionJobState.IN_PROGRESS, null));
    }

    public void setDone(UUID jobId, TranscriptionResult result) {
        jobStatusMap.put(jobId, new TranscriptionJobStatus(TranscriptionJobState.DONE, result));
    }

    public void setError(UUID jobId) {
        jobStatusMap.put(jobId, new TranscriptionJobStatus(TranscriptionJobState.ERROR, null));
    }

    public TranscriptionJobStatus getStatus(UUID jobId) {
        return jobStatusMap.get(jobId);
    }
}
