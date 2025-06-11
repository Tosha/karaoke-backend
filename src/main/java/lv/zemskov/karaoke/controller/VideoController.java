package lv.zemskov.karaoke.controller;

import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.service.subtitle.AssSubtitleService;
import lv.zemskov.karaoke.service.transcription.WhisperService;
import lv.zemskov.karaoke.service.video.FfmpegVideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final FfmpegVideoService videoService;
    private final AssSubtitleService subtitleService;
    private final WhisperService transcriptionService;
    private final SeparationResultRepository separationResultRepository;

    public VideoController(FfmpegVideoService videoService,
                           AssSubtitleService subtitleService,
                           WhisperService transcriptionService,
                           SeparationResultRepository separationResultRepository) {
        this.videoService = videoService;
        this.subtitleService = subtitleService;
        this.transcriptionService = transcriptionService;
        this.separationResultRepository = separationResultRepository;
    }

    @PostMapping("/generate/{trackId}")
    public ResponseEntity<String> generateKaraokeVideo(@PathVariable UUID trackId) {
        try {
            // Get existing results (implement these services)
            TranscriptionResult transcription = transcriptionService.findByTrackId(trackId);
            SeparationResult separation = separationResultRepository.findByTrackId(trackId);

            // Generate ASS subtitles
            String assContent = subtitleService.generateAssSubtitles(transcription);

            // Create video
            Path videoPath = videoService.generateKaraokeVideo(separation, transcription, assContent);

            return ResponseEntity.ok(videoPath.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}