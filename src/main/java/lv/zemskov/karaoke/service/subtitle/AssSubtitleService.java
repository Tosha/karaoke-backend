package lv.zemskov.karaoke.service.subtitle;

import lv.zemskov.karaoke.model.LyricsSegment;
import lv.zemskov.karaoke.model.TranscriptionResult;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssSubtitleService {

    private static final String ASS_HEADER = """
            [Script Info]
            ScriptType: v4.00+
            PlayResX: 384
            PlayResY: 288
            WrapStyle: 0
            ScaledBorderAndShadow: yes
            
            [V4+ Styles]
            Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding
            Style: Default,Arial,24,&H00FFFFFF,&H000000FF,&H00000000,&H00000000,0,0,0,0,100,100,0,0,1,2,2,2,10,10,10,1
            
            [Events]
            Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text
            """;

    public String generateAssSubtitles(TranscriptionResult transcription) {
        StringBuilder assContent = new StringBuilder(ASS_HEADER);

        List<LyricsSegment> segments = transcription.getSegments();
        for (int i = 0; i < segments.size(); i++) {
            LyricsSegment current = segments.get(i);
            LyricsSegment next = i < segments.size() - 1 ? segments.get(i + 1) : null;

            assContent.append(generateAssEvent(current, next));
        }

        return assContent.toString();
    }

    private String generateAssEvent(LyricsSegment segment, LyricsSegment nextSegment) {
        String startTime = formatTime(segment.getStartTimeMs());
        String endTime = nextSegment != null ?
                formatTime(nextSegment.getStartTimeMs()) :
                formatTime(segment.getEndTimeMs() + 1000); // Extra second for last segment

        return String.format("Dialogue: 0,%s,%s,Default,,0,0,0,,%s\\N\n",
                startTime,
                endTime,
                karaokeEffect(segment.getText(), startTime, endTime));
    }

    private String formatTime(long milliseconds) {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds % 3600000) / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        long centiseconds = (milliseconds % 1000) / 10;

        return String.format("%d:%02d:%02d.%02d", hours, minutes, seconds, centiseconds);
    }

    private String karaokeEffect(String text, String startTime, String endTime) {
        // Creates karaoke effect with syllable timing
        return String.format("{\\kf%d}%s",
                calculateDurationMs(startTime, endTime) / 10, // \kf uses centiseconds
                text);
    }

    private long calculateDurationMs(String startTime, String endTime) {
        // Parse time strings and calculate duration
        return convertTimeToMs(endTime) - convertTimeToMs(startTime);
    }

    private long convertTimeToMs(String time) {
        String[] parts = time.split("[:.]");
        return Long.parseLong(parts[0]) * 3600000L + // hours
                Long.parseLong(parts[1]) * 60000L +   // minutes
                Long.parseLong(parts[2]) * 1000L +    // seconds
                Long.parseLong(parts[3]) * 10L;       // centiseconds
    }
}