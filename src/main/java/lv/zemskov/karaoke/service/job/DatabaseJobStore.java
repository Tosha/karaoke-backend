package lv.zemskov.karaoke.service.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lv.zemskov.karaoke.exception.JobProcessingException;
import lv.zemskov.karaoke.model.job.JobEntity;
import lv.zemskov.karaoke.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class DatabaseJobStore implements JobStore<UUID> {

    Logger log = LoggerFactory.getLogger(DatabaseJobStore.class);

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    public DatabaseJobStore(JobRepository jobRepository, ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void create(UUID jobId, JobState state, String message) {
        JobEntity job = new JobEntity();
        job.setJobId(jobId);
        job.setState(state);
        job.setMessage(message);
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public void update(UUID jobId, JobState state, String message) {
        jobRepository.updateState(jobId, state, message, Instant.now());
    }

    @Override
    @Transactional
    public void complete(UUID jobId, Object result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            jobRepository.markCompleted(jobId, resultJson, Instant.now());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize job result", e);
            throw new JobProcessingException("Result serialization failed", e);
        }
    }

    @Override
    @Transactional
    public void fail(UUID jobId, String error) {
        jobRepository.markFailed(jobId, error, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public JobStatus<UUID> getStatus(UUID jobId) {
        return jobRepository.findByJobId(jobId)
                .map(this::toJobStatus)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID jobId) {
        return jobRepository.existsById(jobId);
    }

    private JobStatus<UUID> toJobStatus(JobEntity entity) {
        try {
            Object result = entity.getResult() != null ?
                    objectMapper.readValue(entity.getResult(), Object.class) :
                    null;

            return new JobStatus<>(
                    entity.getJobId(),
                    entity.getState(),
                    entity.getMessage(),
                    result,
                    entity.getError(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize job result", e);
            throw new JobProcessingException("Result deserialization failed", e);
        }
    }
}