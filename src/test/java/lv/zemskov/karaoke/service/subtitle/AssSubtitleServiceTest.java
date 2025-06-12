package lv.zemskov.karaoke.service.subtitle;

import lv.zemskov.karaoke.model.LyricsSegment;
import lv.zemskov.karaoke.model.TranscriptionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AssSubtitleServiceTest {

    @InjectMocks
    private AssSubtitleService assSubtitleService;

    @Test
    void generateAssSubtitles_shouldGenerateValidHeader() {
        TranscriptionResult transcription = new TranscriptionResult();
        transcription.setSegments(new ArrayList<>());

        String result = assSubtitleService.generateAssSubtitles(transcription);

        assertTrue(result.startsWith("[Script Info]"));
        assertTrue(result.contains("[V4+ Styles]"));
        assertTrue(result.contains("[Events]"));
        assertTrue(result.contains("Format: Layer, Start, End, Style, Name"));
    }

    @Test
    void generateAssSubtitles_shouldHandleEmptySegments() {
        TranscriptionResult transcription = new TranscriptionResult();
        transcription.setSegments(new ArrayList<>());

        String result = assSubtitleService.generateAssSubtitles(transcription);

        // Should only contain header with no dialogue events
        assertFalse(result.contains("Dialogue:"));
    }

    @Test
    void generateAssSubtitles_shouldGenerateCorrectDialogueEvents() {
        TranscriptionResult transcription = new TranscriptionResult();
        List<LyricsSegment> segments = new ArrayList<>();

        segments.add(createSegment(1000, 2000, "First line"));
        segments.add(createSegment(2000, 3000, "Second line"));
        transcription.setSegments(segments);

        String result = assSubtitleService.generateAssSubtitles(transcription);

        assertTrue(result.contains("Dialogue: 0,0:00:01.00,0:00:02.00,Default,,0,0,0,,{\\kf100}First line\\N"));
        assertTrue(result.contains("Dialogue: 0,0:00:02.00,0:00:03.00,Default,,0,0,0,,{\\kf100}Second line\\N"));
    }

    @Test
    void generateAssSubtitles_shouldHandleSingleSegment() {
        TranscriptionResult transcription = new TranscriptionResult();
        List<LyricsSegment> segments = new ArrayList<>();
        segments.add(createSegment(1000, 2000, "Only line"));
        transcription.setSegments(segments);

        String result = assSubtitleService.generateAssSubtitles(transcription);

        // Last segment should get extra second duration
        assertTrue(result.contains("Dialogue: 0,0:00:01.00,0:00:03.00,Default,,0,0,0,,{\\kf200}Only line\\N"));
    }

    @Test
    void formatTime_shouldConvertMillisecondsCorrectly() {
        assertEquals("0:00:01.00", assSubtitleService.formatTime(1000));
        assertEquals("1:01:01.01", assSubtitleService.formatTime(3661010));
        assertEquals("0:00:00.10", assSubtitleService.formatTime(100));
    }

    @Test
    void karaokeEffect_shouldGenerateCorrectEffectTag() {
        String effect = assSubtitleService.karaokeEffect("Hello", "0:00:01.00", "0:00:02.00");
        assertEquals("{\\kf100}Hello", effect);
    }

    @Test
    void calculateDurationMs_shouldComputeCorrectDuration() {
        long duration = assSubtitleService.calculateDurationMs("0:00:01.00", "0:00:02.50");
        assertEquals(1500, duration);
    }

    @Test
    void convertTimeToMs_shouldParseTimeStringCorrectly() {
        assertEquals(1000, assSubtitleService.convertTimeToMs("0:00:01.00"));
        assertEquals(3661010, assSubtitleService.convertTimeToMs("1:01:01.01"));
        assertEquals(61000, assSubtitleService.convertTimeToMs("0:01:01.00"));
    }

    private LyricsSegment createSegment(long start, long end, String text) {
        LyricsSegment segment = new LyricsSegment();
        segment.setStartTimeMs(start);
        segment.setEndTimeMs(end);
        segment.setText(text);
        return segment;
    }
}