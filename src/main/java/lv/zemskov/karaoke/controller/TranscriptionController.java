package lv.zemskov.karaoke.controller;

import lombok.extern.slf4j.Slf4j;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.service.transcription.WhisperService;
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
    public ResponseEntity<String> transcribeTrack(@PathVariable UUID trackId) {
        UUID jobId = UUID.randomUUID();
        log.info("Received async transcription request for track {}", trackId);
        transcriptionService.transcribeVocalsAsync(trackId, jobId);
        return ResponseEntity.accepted().body(jobId.toString()); // 202 Accepted
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<TranscriptionResult> getTranscription(
            @PathVariable UUID trackId) {
        return ResponseEntity.ok(transcriptionService.findByTrackId(trackId));
    }
}
