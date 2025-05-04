CREATE TABLE karaoke_schema.track (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    original_file_content BYTEA,
    processed_at TIMESTAMP NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE karaoke_schema.separation_result (
    id BIGSERIAL PRIMARY KEY,
    vocals_content BYTEA,
    accompaniment_content BYTEA,
    processing_time_seconds DOUBLE PRECISION,
    track_id UUID UNIQUE REFERENCES karaoke_schema.track(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);