CREATE TABLE track (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    original_file_path VARCHAR(255),
    processed_at TIMESTAMP NOT NULL,
    file_size BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE separation_result (
    id BIGSERIAL PRIMARY KEY,
    vocals_path VARCHAR(255),
    accompaniment_path VARCHAR(255),
    processing_time_seconds DOUBLE PRECISION,
    track_id UUID REFERENCES track(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);