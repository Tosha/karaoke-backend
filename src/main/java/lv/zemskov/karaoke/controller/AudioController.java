package lv.zemskov.karaoke.controller;

import lombok.extern.slf4j.Slf4j;
import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.service.separation.SpleeterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private static final Logger log = LoggerFactory.getLogger(AudioController.class);
    private final SpleeterService spleeterService;
    private final SeparationResultRepository separationResultRepository;

    public AudioController(SpleeterService spleeterService, SeparationResultRepository separationResultRepository) {
        this.spleeterService = spleeterService;
        this.separationResultRepository = separationResultRepository;
    }

    @PostMapping(value = "/separate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> separateAudio(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Separating file: {}", file.getOriginalFilename());
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            if (!file.getContentType().startsWith("audio/")) {
                return ResponseEntity.badRequest().body("Only audio files are supported");
            }

            var result = spleeterService.processAudio(file);
            log.info("File separation completed: {}", file.getOriginalFilename());
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
            @PathVariable UUID trackId,
            @PathVariable String type) throws IOException {

        SeparationResult result = separationResultRepository.findByTrackId(trackId);

        String filePath;
        String filename;

        if ("vocals".equals(type)) {
            filePath = result.getVocalsPath();
            filename = "vocals.wav";
        } else if ("accompaniment".equals(type)) {
            filePath = result.getAccompanimentPath();
            filename = "accompaniment.wav";
        } else {
            return ResponseEntity.badRequest().build();
        }

        Path path = Paths.get(filePath);
        Resource resource = new InputStreamResource(Files.newInputStream(path));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .contentLength(Files.size(path))
                .body(resource);
    }
}