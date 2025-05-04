package lv.zemskov.karaoke.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "track", schema = "karaoke_schema")
public class Track {
    @Id
    @UuidGenerator
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Lob
    @Column(name = "original_file_content", columnDefinition = "BYTEA")
    private byte[] originalFileContent;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "status", nullable = false)
    private String status; // PROCESSING, COMPLETED, FAILED

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "track", cascade = CascadeType.ALL)
    private SeparationResult separationResult;

    // Constructors
    public Track() {}

    public Track(String originalFilename, byte[] originalFileContent, long fileSize, String status) {
        this.originalFilename = originalFilename;
        this.originalFileContent = originalFileContent;
        this.fileSize = fileSize;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public byte[] getOriginalFileContent() { return originalFileContent; }
    public void setOriginalFileContent(byte[] originalFileContent) { this.originalFileContent = originalFileContent; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public SeparationResult getSeparationResult() { return separationResult; }
    public void setSeparationResult(SeparationResult separationResult) { this.separationResult = separationResult; }
}