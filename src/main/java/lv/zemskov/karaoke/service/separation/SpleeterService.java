package lv.zemskov.karaoke.service.separation;

import jakarta.transaction.Transactional;
import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.Track;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@Transactional
public class SpleeterService {

    private final Path inputDirectory = Paths.get("/input");
    private final Path outputDirectory = Paths.get("/output");

    private final TrackRepository trackRepository;
    private final SeparationResultRepository separationResultRepository;

    public SpleeterService(TrackRepository trackRepository, SeparationResultRepository separationResultRepository) {
        this.trackRepository = trackRepository;
        this.separationResultRepository = separationResultRepository;
    }

    public SeparationResult processAudio(MultipartFile file) throws IOException, InterruptedException {
        // 1. Prepare directories
        Files.createDirectories(inputDirectory);
        Files.createDirectories(outputDirectory);

        // 2. Create and save Track entity
        Track track = new Track();
        track.setOriginalFilename(file.getOriginalFilename());
        track.setProcessedAt(LocalDateTime.now());
        track.setFileSize(file.getSize());
        track.setStatus("PROCESSING");
        track = trackRepository.save(track);

        try {
            // 3. Store original file
            String fileId = track.getId().toString();
            Path inputFile = inputDirectory.resolve(fileId + ".mp3");
            file.transferTo(inputFile);
            track.setOriginalFilePath(inputFile.toString());
            trackRepository.save(track);

            // 4. Process with Spleeter
            Path outputDir = outputDirectory.resolve(fileId);
            Files.createDirectories(outputDir);

            Process process = new ProcessBuilder()
                    .command("spleeter", "separate", "-p", "spleeter:2stems",
                            "-o", outputDirectory.toString(), inputFile.toString())
                    .inheritIO()
                    .start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Spleeter processing failed");
            }

            // 5. Create and save results
            Path vocalsFile = outputDir.resolve("vocals.wav");
            Path accompanimentFile = outputDir.resolve("accompaniment.wav");

            SeparationResult result = new SeparationResult();
            result.setVocalsPath(vocalsFile.toString());
            result.setAccompanimentPath(accompanimentFile.toString());
            result.setProcessingTimeSeconds(1); //TODO: Add calculation of processing time
            result.setTrack(track);
            separationResultRepository.save(result);

            // 6. Update track status
            track.setStatus("COMPLETED");
            trackRepository.save(track);

            return result;
        } catch (Exception e) {
            track.setStatus("FAILED");
            trackRepository.save(track);
            throw e;
        }
    }
}