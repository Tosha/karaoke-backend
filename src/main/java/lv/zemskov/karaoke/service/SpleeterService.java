package lv.zemskov.karaoke.service;

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
public class SpleeterService {
    private static final Logger logger = LoggerFactory.getLogger(SpleeterService.class);

    @Value("${spleeter.input-dir:/app/input}")
    private String inputDir;

    @Value("${spleeter.output-dir:/app/output}")
    private String outputDir;

    public SeparationResult processAudio(MultipartFile multipartFile) throws IOException, InterruptedException {
        // Validate input file
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("Only MP3 files are supported");
        }

        // Generate unique filename
        String uniqueId = UUID.randomUUID().toString();
        String sanitizedFilename = uniqueId + ".mp3";
        Path inputPath = Paths.get(inputDir, sanitizedFilename);

        // Ensure directories exist
        Files.createDirectories(Paths.get(inputDir));
        Files.createDirectories(Paths.get(outputDir));

        // Save uploaded file
        multipartFile.transferTo(inputPath);
        logger.info("Saved input file to: {}", inputPath);

        // Prepare output directory
        String outputSubdir = uniqueId;
        Path outputPath = Paths.get(outputDir, outputSubdir);
        Files.createDirectories(outputPath);

        // Execute Spleeter
        Process process = new ProcessBuilder()
                .command("spleeter", "separate",
                        "-p", "spleeter:2stems",
                        "-o", outputDir,
                        inputPath.toString())
                .inheritIO()
                .start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Spleeter process failed with exit code " + exitCode);
        }

        logger.info("Successfully processed audio. Results in: {}", outputPath);

        return new SeparationResult(
                outputSubdir,
                outputPath.resolve("vocals.wav").toString(),
                outputPath.resolve("accompaniment.wav").toString()
        );
    }

    public record SeparationResult(
            String trackId,
            String vocalsPath,
            String accompanimentPath
    ) {}
}