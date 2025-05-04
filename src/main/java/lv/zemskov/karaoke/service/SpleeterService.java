package lv.zemskov.karaoke.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class SpleeterService {
    private static final Logger logger = LoggerFactory.getLogger(SpleeterService.class);

    public SeparationResult processAudio(MultipartFile file) throws IOException, InterruptedException {
        // 1. Validate input
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("Only MP3 files are supported");
        }

        // 2. Prepare paths (using Spleeter's default locations)
        String fileId = UUID.randomUUID().toString();
        Path inputFile = Paths.get("/input", fileId + ".mp3");
        Path outputDir = Paths.get("/output", fileId);

        Files.createDirectories(inputFile.getParent());
        Files.createDirectories(outputDir);

        // 3. Save uploaded file
        file.transferTo(inputFile);
        logger.info("Audio saved for processing: {}", inputFile);

        // 4. Execute Spleeter
        Process process = new ProcessBuilder()
                .command("spleeter", "separate",
                        "-p", "spleeter:2stems",
                        "-o", "/output",
                        inputFile.toString())
                .inheritIO()
                .start();

        // 5. Monitor process
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(String.format(
                    "Spleeter failed with exit code %d. Input: %s",
                    exitCode, inputFile));
        }

        // 6. Verify output
        Path vocals = outputDir.resolve("vocals.wav");
        Path accompaniment = outputDir.resolve("accompaniment.wav");

        if (!Files.exists(vocals) || !Files.exists(accompaniment)) {
            throw new IOException(String.format(
                    "Output files not generated. Check Spleeter logs. Output dir: %s",
                    outputDir));
        }

        logger.info("Successfully separated audio. Vocals: {}, Accompaniment: {}",
                vocals, accompaniment);

        return new SeparationResult(
                fileId,
                vocals.toString(),
                accompaniment.toString()
        );
    }

    public record SeparationResult(
            String trackId,
            String vocalsPath,
            String accompanimentPath
    ) {}
}