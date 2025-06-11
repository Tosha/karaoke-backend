package lv.zemskov.karaoke.service.video;

import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.TranscriptionResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FfmpegVideoService {

    public Path generateKaraokeVideo(SeparationResult separation,
                                     TranscriptionResult transcription,
                                     String assSubtitles) throws IOException {
        Path vocalsPath = Paths.get(separation.getVocalsPath());
        Path accompanimentPath = Paths.get(separation.getAccompanimentPath());
        Path outputPath = Paths.get("/output", separation.getTrack().getId().toString(), "karaoke.mp4");
        Path assPath = Paths.get("/tmp", separation.getTrack().getId().toString() + ".ass");

        Files.createDirectories(outputPath.getParent());
        Files.writeString(assPath, assSubtitles);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y", // Overwrite without asking
                "-i", accompanimentPath.toString(),
                "-i", vocalsPath.toString(),
                "-vf", String.format("ass=%s", assPath.toString()),
                "-c:v", "libx264",
                "-preset", "fast",
                "-crf", "22",
                "-c:a", "aac",
                "-b:a", "192k",
                "-filter_complex", "[1:a]volume=1.5[vocals];[0:a][vocals]amix=inputs=2:duration=longest",
                outputPath.toString()
        );

        Process process = pb.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg failed with code: " + exitCode);
            }
            return outputPath;
        } catch (InterruptedException e) {
            throw new IOException("Video generation interrupted", e);
        } finally {
            Files.deleteIfExists(assPath);
        }
    }
}