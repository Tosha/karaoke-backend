package lv.zemskov.karaoke.controller;

import lv.zemskov.karaoke.service.SpleeterService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private final SpleeterService spleeterService;

    public AudioController(SpleeterService spleeterService) {
        this.spleeterService = spleeterService;
    }

    @PostMapping(value = "/separate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> separateAudio(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            if (!file.getContentType().startsWith("audio/")) {
                return ResponseEntity.badRequest().body("Only audio files are supported");
            }

            var result = spleeterService.processAudio(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error processing audio: " + e.getMessage());
        }
    }

    @GetMapping("/download/{trackId}/{type}")
    public ResponseEntity<Resource> downloadTrack(
            @PathVariable String trackId,
            @PathVariable String type) throws IOException {

        if (!"vocals".equals(type) && !"accompaniment".equals(type)) {
            return ResponseEntity.badRequest().build();
        }

        String filePath = String.format("/tmp/output/%s/%s.wav", trackId, type);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"%s.wav\"".formatted(type))
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(resource);
    }
}