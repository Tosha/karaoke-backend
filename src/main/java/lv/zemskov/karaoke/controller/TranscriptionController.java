package lv.zemskov.karaoke.controller;

import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.service.WhisperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transcription")
public class TranscriptionController {

    private final WhisperService transcriptionService;

    public TranscriptionController(WhisperService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping("/{trackId}")
    public ResponseEntity<TranscriptionResult> transcribeTrack(
            @PathVariable UUID trackId) {
        try {
            TranscriptionResult result = transcriptionService.transcribeVocals(trackId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{trackId}")
    public ResponseEntity<TranscriptionResult> getTranscription(
            @PathVariable Long trackId) {
        return ResponseEntity.ok(transcriptionService.findByTrackId(trackId));
    }
}
