package lv.zemskov.karaoke.model;

import jakarta.persistence.*;

@Entity
@Table(name = "separation_result", schema = "karaoke_schema")
public class SeparationResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vocalsPath;      // Filesystem path
    private String accompanimentPath; // Filesystem path
    private double processingTimeSeconds;

    @OneToOne
    @JoinColumn(name = "track_id")
    private Track track;

    public SeparationResult(String vocalsPath, String accompanimentPath, double processingTimeSeconds, Track track) {
        this.vocalsPath = vocalsPath;
        this.accompanimentPath = accompanimentPath;
        this.processingTimeSeconds = processingTimeSeconds;
        this.track = track;
    }

    public SeparationResult() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVocalsPath() {
        return vocalsPath;
    }

    public void setVocalsPath(String vocalsPath) {
        this.vocalsPath = vocalsPath;
    }

    public String getAccompanimentPath() {
        return accompanimentPath;
    }

    public void setAccompanimentPath(String accompanimentPath) {
        this.accompanimentPath = accompanimentPath;
    }

    public double getProcessingTimeSeconds() {
        return processingTimeSeconds;
    }

    public void setProcessingTimeSeconds(double processingTimeSeconds) {
        this.processingTimeSeconds = processingTimeSeconds;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}