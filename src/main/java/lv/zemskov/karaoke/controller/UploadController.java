package lv.zemskov.karaoke.controller;

import lv.zemskov.karaoke.model.Track;
import lv.zemskov.karaoke.service.AudioUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final AudioUploadService uploadService;

    public UploadController(AudioUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public ResponseEntity<Track> uploadAudio(@RequestParam MultipartFile file) throws IOException {
        Track track = uploadService.processUpload(file);
        return ResponseEntity.ok(track);
    }
}