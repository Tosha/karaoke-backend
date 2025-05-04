package lv.zemskov.karaoke.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "separation_result", schema = "karaoke_schema")
public class SeparationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "vocals_content", columnDefinition = "BYTEA")
    private byte[] vocalsContent;

    @Lob
    @Column(name = "accompaniment_content", columnDefinition = "BYTEA")
    private byte[] accompanimentContent;

    @Column(name = "processing_time_seconds")
    private Double processingTimeSeconds;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "track_id", referencedColumnName = "id")
    private Track track;

    // Constructors
    public SeparationResult() {}

    public SeparationResult(byte[] vocalsContent, byte[] accompanimentContent, Track track) {
        this.vocalsContent = vocalsContent;
        this.accompanimentContent = accompanimentContent;
        this.track = track;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public byte[] getVocalsContent() { return vocalsContent; }
    public void setVocalsContent(byte[] vocalsContent) { this.vocalsContent = vocalsContent; }
    public byte[] getAccompanimentContent() { return accompanimentContent; }
    public void setAccompanimentContent(byte[] accompanimentContent) { this.accompanimentContent = accompanimentContent; }
    public Double getProcessingTimeSeconds() { return processingTimeSeconds; }
    public void setProcessingTimeSeconds(Double processingTimeSeconds) { this.processingTimeSeconds = processingTimeSeconds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }
}