package lv.zemskov.karaoke.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "track", schema = "karaoke_schema")
public class Track {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String originalFilename;
    private String originalFilePath;  // Stores filesystem path
    private LocalDateTime processedAt;
    private long fileSize;
    private String status; // PROCESSING, COMPLETED, FAILED

    @OneToOne(mappedBy = "track", cascade = CascadeType.ALL)
    private SeparationResult separationResult;

    public Track(String originalFilename, String originalFilePath, LocalDateTime processedAt, long fileSize, String status, SeparationResult separationResult) {
        this.originalFilename = originalFilename;
        this.originalFilePath = originalFilePath;
        this.processedAt = processedAt;
        this.fileSize = fileSize;
        this.status = status;
        this.separationResult = separationResult;
    }

    public Track() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SeparationResult getSeparationResult() {
        return separationResult;
    }

    public void setSeparationResult(SeparationResult separationResult) {
        this.separationResult = separationResult;
    }
}