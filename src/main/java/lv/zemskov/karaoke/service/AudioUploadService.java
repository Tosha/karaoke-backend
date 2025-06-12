package lv.zemskov.karaoke.service;

import lv.zemskov.karaoke.exception.InvalidAudioFileException;
import lv.zemskov.karaoke.model.Track;
import lv.zemskov.karaoke.repository.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AudioUploadService {
    private static final Logger log = LoggerFactory.getLogger(AudioUploadService.class);

    @Value("${audio.upload.directory:/uploads}")
    private String uploadDirectory;

    private final TrackRepository trackRepository;

    public AudioUploadService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public Track processUpload(MultipartFile file) throws IOException {
        // 1. Validate file
        validateAudioFile(file);

        // 2. Create track entity
        Track track = new Track();
        track.setOriginalFilename(file.getOriginalFilename());
        track.setStatus("UPLOADED");
        track = trackRepository.save(track);

        // 3. Store file
        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileId = track.getId().toString();
        Path destination = uploadPath.resolve(fileId + ".mp3");
        file.transferTo(destination);

        // 4. Update track with path
        track.setOriginalFilePath(destination.toString());
        track.setFileSize(file.getSize());
        trackRepository.save(track);

        log.info("Audio uploaded successfully: {}", destination);
        return track;
    }

    private void validateAudioFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidAudioFileException("Uploaded file is empty");
        }

        if (!file.getContentType().startsWith("audio/")) {
            throw new InvalidAudioFileException("Only audio files are supported");
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new InvalidAudioFileException("File size exceeds 50MB limit");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw new InvalidAudioFileException("Only MP3 files are supported");
        }
    }

    public Path getAudioPath(UUID trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));
        return Paths.get(track.getOriginalFilePath());
    }
}