package lv.zemskov.karaoke.service.transcription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.Track;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.repository.TranscriptionResultRepository;
import lv.zemskov.karaoke.service.job.JobState;
import lv.zemskov.karaoke.service.job.JobStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhisperServiceTest {

    @Mock
    private SeparationResultRepository separationResultRepository;

    @Mock
    private TranscriptionResultRepository transcriptionResultRepository;

    @Mock
    private JobStore<UUID> jobStore;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WhisperService whisperService;

    private final UUID trackId = UUID.randomUUID();
    private final UUID jobId = UUID.randomUUID();
    private final Track testTrack = new Track();
    private final SeparationResult testSeparation = new SeparationResult();

    @BeforeEach
    void setUp() {
        testTrack.setId(trackId);
        testSeparation.setTrack(testTrack);
        testSeparation.setVocalsPath("/path/to/vocals.wav");
    }

    @Test
    void transcribeVocalsAsync_shouldHandleSuccessCase() {
        when(separationResultRepository.findByTrackId(trackId)).thenReturn(testSeparation);

        // Since it's void, we just verify interactions
        whisperService.transcribeVocalsAsync(trackId, jobId);

        verify(jobStore).update(jobId, JobState.TRANSCRIBING, "Starting audio transcription");
        // Additional verifications would need to be done if we could capture the async result
    }

    @Test
    void transcribeVocalsAsync_shouldHandleFailureCase() {
        when(separationResultRepository.findByTrackId(trackId)).thenThrow(new RuntimeException("Test error"));

        whisperService.transcribeVocalsAsync(trackId, jobId);

        verify(jobStore).fail(jobId, "Transcription error: Test error");
    }

    @Test
    void transcribeVocals_shouldReturnTranscriptionResult() throws Exception {
        when(separationResultRepository.findByTrackId(trackId)).thenReturn(testSeparation);

        // Mock process execution
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(5, TimeUnit.MINUTES)).thenReturn(true);
        when(mockProcess.exitValue()).thenReturn(0);

        try (MockedStatic<ProcessBuilder> pbStatic = mockStatic(ProcessBuilder.class)) {
            ProcessBuilder mockPb = mock(ProcessBuilder.class);
            pbStatic.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
            when(mockPb.start()).thenReturn(mockProcess);

            // Mock file operations
            Path mockPath = mock(Path.class);
            when(mockPath.getFileName()).thenReturn(Paths.get("vocals.wav"));
            try (MockedStatic<Paths> pathsStatic = mockStatic(Paths.class)) {
                pathsStatic.when(() -> Paths.get(anyString())).thenReturn(mockPath);

                // Mock JSON parsing
                JsonNode mockNode = mock(JsonNode.class);
                when(objectMapper.readTree(any(File.class))).thenReturn(mockNode);
                when(objectMapper.writeValueAsString(mockNode)).thenReturn("{}");

                TranscriptionResult result = whisperService.transcribeVocals(trackId);

                assertNotNull(result);
                assertEquals(testTrack, result.getTrack());
                verify(transcriptionResultRepository).save(result);
            }
        }
    }

    @Test
    void transcribeVocals_shouldThrowWhenProcessFails() throws Exception {
        when(separationResultRepository.findByTrackId(trackId)).thenReturn(testSeparation);

        // Mock process that will fail
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(5, TimeUnit.MINUTES)).thenReturn(true);
        when(mockProcess.exitValue()).thenReturn(1);  // Non-zero exit code
        when(mockProcess.getInputStream())
                .thenReturn(new ByteArrayInputStream("error output".getBytes()));

        try (MockedStatic<ProcessBuilder> pbStatic = mockStatic(ProcessBuilder.class)) {
            ProcessBuilder mockPb = mock(ProcessBuilder.class);
            // Correct way to mock the ProcessBuilder constructor
            pbStatic.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
            when(mockPb.start()).thenReturn(mockProcess);

            // Mock Paths.get() if needed
            try (MockedStatic<Paths> pathsStatic = mockStatic(Paths.class)) {
                Path mockPath = mock(Path.class);
                when(mockPath.getFileName()).thenReturn(Paths.get("vocals.wav"));
                pathsStatic.when(() -> Paths.get(anyString())).thenReturn(mockPath);

                // Verify the exception is thrown
                IOException exception = assertThrows(IOException.class,
                        () -> whisperService.transcribeVocals(trackId));

                // Verify the error message contains the process output
                assertTrue(exception.getMessage().contains("error output"));
            }
        }
    }

    @Test
    void transcribeVocals_shouldThrowWhenTimeout() throws Exception {
        when(separationResultRepository.findByTrackId(trackId)).thenReturn(testSeparation);

        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor(5, TimeUnit.MINUTES)).thenReturn(false);

        try (MockedStatic<ProcessBuilder> pbStatic = mockStatic(ProcessBuilder.class)) {
            ProcessBuilder mockPb = mock(ProcessBuilder.class);
            pbStatic.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
            when(mockPb.start()).thenReturn(mockProcess);

            assertThrows(RuntimeException.class, () -> whisperService.transcribeVocals(trackId));
        }
    }

    @Test
    void parseTranscriptionResult_shouldParseJsonCorrectly() throws Exception {
        Path audioFile = Paths.get("vocals.wav");
        JsonNode mockRootNode = mock(JsonNode.class);
        JsonNode mockLanguage = mock(JsonNode.class);
        JsonNode mockConfidence = mock(JsonNode.class);
        JsonNode mockSegment = mock(JsonNode.class);
        JsonNode mockText = mock(JsonNode.class);
        JsonNode mockStart = mock(JsonNode.class);
        JsonNode mockEnd = mock(JsonNode.class);
        JsonNode mockSegmentConfidence = mock(JsonNode.class);

        when(objectMapper.readTree(any(File.class))).thenReturn(mockRootNode);
        when(mockRootNode.path("language")).thenReturn(mockLanguage);
        when(mockLanguage.asText()).thenReturn("en");
        when(mockRootNode.path("confidence")).thenReturn(mockConfidence);
        when(mockConfidence.asDouble()).thenReturn(0.95);
        when(mockRootNode.path("segments")).thenReturn(mock(JsonNode.class));
        when(mockRootNode.path("segments").iterator()).thenReturn(
                Collections.singletonList(mockSegment).iterator());

        // Mock segment values
        when(mockSegment.path("text")).thenReturn(mockText);
        when(mockText.asText()).thenReturn("test lyrics");
        when(mockSegment.path("start")).thenReturn(mockStart);
        when(mockStart.asDouble()).thenReturn(1.0);
        when(mockSegment.path("end")).thenReturn(mockEnd);
        when(mockEnd.asDouble()).thenReturn(2.0);
        when(mockSegment.path("confidence")).thenReturn(mockSegmentConfidence);
        when(mockSegmentConfidence.asDouble()).thenReturn(0.9);

        when(objectMapper.writeValueAsString(mockRootNode)).thenReturn("{}");

        try (MockedStatic<Files> filesStatic = mockStatic(Files.class)) {
            filesStatic.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            TranscriptionResult result = whisperService.parseTranscriptionResult(audioFile, testTrack);

            assertEquals("en", result.getLanguageCode());
            assertEquals(0.95f, result.getConfidenceScore());
            assertEquals(1, result.getSegments().size());
            assertEquals("test lyrics", result.getSegments().get(0).getText());
        }
    }

    @Test
    void findByTrackId_shouldDelegateToRepository() {
        TranscriptionResult expected = new TranscriptionResult();
        when(transcriptionResultRepository.findByTrackId(trackId)).thenReturn(expected);

        TranscriptionResult actual = whisperService.findByTrackId(trackId);

        assertEquals(expected, actual);
        verify(transcriptionResultRepository).findByTrackId(trackId);
    }
}