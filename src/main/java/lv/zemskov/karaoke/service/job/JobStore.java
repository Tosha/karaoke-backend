package lv.zemskov.karaoke.service.job;

public interface JobStore<T> {
    void create(T jobId, JobState state, String message);
    void update(T jobId, JobState state, String message);
    void complete(T jobId, Object result);
    void fail(T jobId, String error);
    JobStatus<T> getStatus(T jobId);
    boolean exists(T jobId);
}