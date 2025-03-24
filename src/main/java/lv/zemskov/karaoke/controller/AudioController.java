package lv.zemskov.karaoke.controller;

import lv.zemskov.karaoke.service.SpleeterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/audio")
public class AudioController {

    private final SpleeterService spleeterService;

    public AudioController(SpleeterService spleeterService) {
        this.spleeterService = spleeterService;
    }

    @PostMapping("/separate")
    public ResponseEntity<String> separateAudio(@RequestParam("file") MultipartFile file) {
        try {
            String outputPath = spleeterService.processAudio(file);
            return ResponseEntity.ok("Processed file saved at: " + outputPath);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing audio: " + e.getMessage());
        }
    }
}
