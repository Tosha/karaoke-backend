CREATE TABLE karaoke_schema.transcription_result (
    id BIGSERIAL PRIMARY KEY,
    track_id UUID REFERENCES karaoke_schema.track(id),
    raw_json TEXT,
    language_code VARCHAR(10),
    confidence_score FLOAT,
    processing_time_ms BIGINT,
    created_at TIMESTAMP
);

CREATE TABLE karaoke_schema.lyrics_segment (
    id BIGSERIAL PRIMARY KEY,
    transcription_id BIGINT REFERENCES karaoke_schema.transcription_result(id),
    text TEXT,
    start_time_ms BIGINT,
    end_time_ms BIGINT,
    confidence FLOAT
);

CREATE INDEX idx_transcription_track ON karaoke_schema.transcription_result(track_id);