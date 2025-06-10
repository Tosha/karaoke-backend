package lv.zemskov.karaoke.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "transcription_result", schema = "karaoke_schema")
public class TranscriptionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "track_id")
    private Track track;

    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawWhisperOutput;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "confidence_score")
    private Float confidenceScore;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "transcription", cascade = CascadeType.ALL)
    private List<LyricsSegment> segments;

    public TranscriptionResult(Long id, Track track, String rawWhisperOutput, String languageCode, Float confidenceScore, Long processingTimeMs, LocalDateTime createdAt, List<LyricsSegment> segments) {
        this.id = id;
        this.track = track;
        this.rawWhisperOutput = rawWhisperOutput;
        this.languageCode = languageCode;
        this.confidenceScore = confidenceScore;
        this.processingTimeMs = processingTimeMs;
        this.createdAt = createdAt;
        this.segments = segments;
    }

    public TranscriptionResult() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }

    public String getRawWhisperOutput() {
        return rawWhisperOutput;
    }

    public void setRawWhisperOutput(String rawWhisperOutput) {
        this.rawWhisperOutput = rawWhisperOutput;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Float getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Float confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LyricsSegment> getSegments() {
        return segments;
    }

    public void setSegments(List<LyricsSegment> segments) {
        this.segments = segments;
    }
}


