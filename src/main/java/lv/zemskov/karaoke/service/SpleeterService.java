package lv.zemskov.karaoke.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SpleeterService {

    public String processAudio(MultipartFile multipartFile) throws IOException, InterruptedException {
        // Get project root
        String projectRoot = System.getProperty("user.dir");

        // Define input and output directories
        Path inputDir = Paths.get(projectRoot, "target", "input");
        Path outputDir = Paths.get(projectRoot, "target", "result");

        // Ensure directories exist
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);

        // Save uploaded file to input dir
        String originalFileName = multipartFile.getOriginalFilename();
        File inputFile = new File(inputDir.toFile(), originalFileName);
        multipartFile.transferTo(inputFile);

        // Build Docker command
        String dockerCommand = String.format(
                "docker run --rm " +
                        "-v \"%s:/input\" " +
                        "-v \"%s:/output\" " +
                        "researchdeezer/spleeter separate -p spleeter:2stems -o /output -i /input/%s",
                inputDir.toAbsolutePath(),
                outputDir.toAbsolutePath(),
                originalFileName
        );

        // Execute Docker command
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("cmd", "/c", dockerCommand); // If on Windows, use ["cmd", "/c", dockerCommand]
        builder.inheritIO();
        Process process = builder.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Spleeter Docker process failed with exit code " + exitCode);
        }

        // Return result path (relative)
        return Paths.get("target", "result", originalFileName.replace(".mp3", "")).toString();
    }
}
