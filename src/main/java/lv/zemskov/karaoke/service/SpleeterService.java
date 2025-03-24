package lv.zemskov.karaoke.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class SpleeterService {

    public String processAudio(MultipartFile file) throws IOException, InterruptedException {
        File tempFile = File.createTempFile("upload", ".mp3");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        String outputDir = tempFile.getParent() + "/output";
        new File(outputDir).mkdir();

        Process process = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", tempFile.getParent() + ":/input",
                "-v", outputDir + ":/output",
                "deezer/spleeter:latest",
                "separate", "-i", "/input/" + tempFile.getName(), "-o", "/output"
        ).inheritIO().start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Spleeter failed with exit code: " + exitCode);
        }

        return outputDir;
    }
}
