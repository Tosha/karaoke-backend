package lv.zemskov.karaoke.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lyrics_segment", schema = "karaoke_schema")
public class LyricsSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "transcription_id")
    private TranscriptionResult transcription;

    @Column(name = "text")
    private String text;

    @Column(name = "start_time_ms")
    private Long startTimeMs;

    @Column(name = "end_time_ms")
    private Long endTimeMs;

    @Column(name = "confidence")
    private Float confidence;

    public LyricsSegment(Float confidence, TranscriptionResult transcription, String text, Long startTimeMs, Long endTimeMs, Long id) {
        this.confidence = confidence;
        this.transcription = transcription;
        this.text = text;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.id = id;
    }

    public LyricsSegment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TranscriptionResult getTranscription() {
        return transcription;
    }

    public void setTranscription(TranscriptionResult transcription) {
        this.transcription = transcription;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getStartTimeMs() {
        return startTimeMs;
    }

    public void setStartTimeMs(Long startTimeMs) {
        this.startTimeMs = startTimeMs;
    }

    public Long getEndTimeMs() {
        return endTimeMs;
    }

    public void setEndTimeMs(Long endTimeMs) {
        this.endTimeMs = endTimeMs;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }
}