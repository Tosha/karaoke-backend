package lv.zemskov.karaoke.controller;

import lv.zemskov.karaoke.service.SpleeterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

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
            String resultPath = spleeterService.processAudio(file);
            return ResponseEntity.ok("Audio separated successfully. Files saved to: " + resultPath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error separating audio: " + e.getMessage());
        }
    }
}
