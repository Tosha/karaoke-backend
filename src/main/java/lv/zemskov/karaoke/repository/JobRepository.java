package lv.zemskov.karaoke.repository;

import lv.zemskov.karaoke.model.job.JobEntity;
import lv.zemskov.karaoke.service.job.JobState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository extends JpaRepository<JobEntity, UUID> {

    @Transactional
    @Modifying
    @Query("UPDATE JobEntity j SET " +
            "j.state = :state, " +
            "j.message = :message, " +
            "j.updatedAt = :now " +
            "WHERE j.jobId = :jobId")
    void updateState(UUID jobId, JobState state, String message, Instant now);

    @Transactional
    @Modifying
    @Query("UPDATE JobEntity j SET " +
            "j.state = 'COMPLETED', " +
            "j.result = :result, " +
            "j.updatedAt = :now " +
            "WHERE j.jobId = :jobId")
    void markCompleted(UUID jobId, String result, Instant now);

    @Transactional
    @Modifying
    @Query("UPDATE JobEntity j SET " +
            "j.state = 'FAILED', " +
            "j.error = :error, " +
            "j.updatedAt = :now " +
            "WHERE j.jobId = :jobId")
    void markFailed(UUID jobId, String error, Instant now);

    Optional<JobEntity> findByJobId(UUID jobId);
}