CREATE TABLE karaoke_schema.jobs (
    job_id UUID PRIMARY KEY,
    state VARCHAR(20) NOT NULL,
    message TEXT,
    result TEXT,
    error TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_jobs_state ON karaoke_schema.jobs(state);
CREATE INDEX idx_jobs_created ON karaoke_schema.jobs(created_at);