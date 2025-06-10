package lv.zemskov.karaoke.service.subtitle;

import lv.zemskov.karaoke.model.LyricsSegment;
import lv.zemskov.karaoke.model.TranscriptionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class AssSubtitleService {

    private static final Logger log = LoggerFactory.getLogger(AssSubtitleService.class);
    private final Path outputDirectory = Paths.get("/output/subtitles");

    public AssSubtitleService() {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            log.error("Failed to create subtitle output directory: {}", e.getMessage());
            throw new RuntimeException("Could not initialize subtitle output directory", e);
        }
    }

    public Path generateAssFile(TranscriptionResult transcriptionResult) throws IOException {
        UUID trackId = transcriptionResult.getTrack().getId();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String assFilename = String.format("%s_%s.ass", trackId, timestamp);
        Path assFilePath = outputDirectory.resolve(assFilename);

        StringBuilder assContent = new StringBuilder();
        // ASS Header
        assContent.append("[Script Info]\n")
                .append("Title: Karaoke Subtitles\n")
                .append("ScriptType: v4.00+\n")
                .append("WrapStyle: 0\n")
                .append("ScaledBorderAndShadow: yes\n")
                .append("YCbCr Matrix: TV.601\n")
                .append("PlayResX: 1920\n")
                .append("PlayResY: 1080\n\n");

        // Styles Section
        assContent.append("[V4+ Styles]\n")
                .append("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n")
                .append("Style: Karaoke,Arial,48,&H00FFFFFF,&H0000FFFF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,2,2,2,10,10,30,1\n\n");

        // Events Section
        assContent.append("[Events]\n")
                .append("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n");

        // Generate karaoke events
        for (LyricsSegment segment : transcriptionResult.getSegments()) {
            String startTime = formatAssTime(segment.getStartTimeMs());
            String endTime = formatAssTime(segment.getEndTimeMs());
            String text = segment.getText().replace("\n", "\\N").trim();

            // Simple karaoke effect: highlight text as it plays
            String karaokeText = String.format("{\\k%d}%s",
                    (segment.getEndTimeMs() - segment.getStartTimeMs()) / 10, text);

            assContent.append(String.format("Dialogue: 0,%s,%s,Karaoke,,0,0,0,,%s\n",
                    startTime, endTime, karaokeText));
        }

        // Write to file
        Files.writeString(assFilePath, assContent.toString());
        log.info("Generated ASS subtitle file: {}", assFilePath);

        return assFilePath;
    }

    private String formatAssTime(long milliseconds) {
        long hours = milliseconds / 3_600_000;
        milliseconds %= 3_600_000;
        long minutes = milliseconds / 60_000;
        milliseconds %= 60_000;
        long seconds = milliseconds / 1_000;
        long centiseconds = (milliseconds % 1_000) / 10;

        return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds);
    }
}