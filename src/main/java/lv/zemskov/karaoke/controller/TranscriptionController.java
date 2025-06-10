package lv.zemskov.karaoke.controller;

import lombok.extern.slf4j.Slf4j;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.service.WhisperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/transcription")
public class TranscriptionController {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionController.class);
    private final WhisperService transcriptionService;

    public TranscriptionController(WhisperService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/{trackId}")
    public ResponseEntity<TranscriptionResult> transcribeTrack(
            @PathVariable UUID trackId) {
        try {
            log.info("Transcription requested for track id: {}", trackId);
            TranscriptionResult result = transcriptionService.transcribeVocals(trackId);
            log.info("Track transcription completed for track: {}", trackId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during transcription of track: {}", trackId);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<TranscriptionResult> getTranscription(
            @PathVariable Long trackId) {
        return ResponseEntity.ok(transcriptionService.findByTrackId(trackId));
    }
}
