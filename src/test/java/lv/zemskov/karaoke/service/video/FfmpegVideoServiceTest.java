package lv.zemskov.karaoke.service.video;

import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FfmpegVideoServiceTest {

    @InjectMocks
    private FfmpegVideoService ffmpegVideoService;

    private final UUID trackId = UUID.randomUUID();
    private final Track testTrack = new Track(String.valueOf(trackId), null, null, 50, null, null);
    private final SeparationResult testSeparation = new SeparationResult();
    private final TranscriptionResult testTranscription = new TranscriptionResult();
    private final String testSubtitles = "[Script Info]\nScriptType: v4.00+";

    @BeforeEach
    void setUp() {
        testSeparation.setTrack(testTrack);
        testSeparation.setVocalsPath("/path/to/vocals.wav");
        testSeparation.setAccompanimentPath("/path/to/accompaniment.wav");
    }

    @Test
    void generateKaraokeVideo_shouldCreateVideoSuccessfully() throws Exception {
        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            // Mock Paths
            Path mockVocalsPath = mock(Path.class);
            Path mockAccompanimentPath = mock(Path.class);
            Path mockOutputPath = mock(Path.class);
            Path mockAssPath = mock(Path.class);
            Path mockParentDir = mock(Path.class);

            when(Paths.get("/path/to/vocals.wav")).thenReturn(mockVocalsPath);
            when(Paths.get("/path/to/accompaniment.wav")).thenReturn(mockAccompanimentPath);
            when(Paths.get("/output/" + trackId + "/karaoke.mp4")).thenReturn(mockOutputPath);
            when(Paths.get("/tmp/" + trackId + ".ass")).thenReturn(mockAssPath);
            when(mockOutputPath.getParent()).thenReturn(mockParentDir);

            // Mock Files
            filesMock.when(() -> Files.createDirectories(mockParentDir)).thenReturn(mockParentDir);
            filesMock.when(() -> Files.writeString(mockAssPath, testSubtitles)).thenReturn(mockAssPath);
            filesMock.when(() -> Files.deleteIfExists(mockAssPath)).thenReturn(true);

            // Mock Process
            Process mockProcess = mock(Process.class);
            when(mockProcess.waitFor()).thenReturn(0);

            try (MockedStatic<ProcessBuilder> pbMock = mockStatic(ProcessBuilder.class)) {
                ProcessBuilder mockPb = mock(ProcessBuilder.class);
                pbMock.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
                when(mockPb.start()).thenReturn(mockProcess);

                Path result = ffmpegVideoService.generateKaraokeVideo(
                        testSeparation,
                        testTranscription,
                        testSubtitles
                );

                assertEquals(mockOutputPath, result);
                filesMock.verify(() -> Files.deleteIfExists(mockAssPath));
            }
        }
    }

    @Test
    void generateKaraokeVideo_shouldThrowWhenFfmpegFails() throws Exception {
        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            // Setup path mocks
            Path mockAssPath = mock(Path.class);
            when(Paths.get(anyString())).thenReturn(mock(Path.class));
            when(Paths.get("/tmp/" + trackId + ".ass")).thenReturn(mockAssPath);

            // Mock Process
            Process mockProcess = mock(Process.class);
            when(mockProcess.waitFor()).thenReturn(1); // Non-zero exit code

            try (MockedStatic<ProcessBuilder> pbMock = mockStatic(ProcessBuilder.class)) {
                ProcessBuilder mockPb = mock(ProcessBuilder.class);
                pbMock.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
                when(mockPb.start()).thenReturn(mockProcess);

                assertThrows(IOException.class, () ->
                        ffmpegVideoService.generateKaraokeVideo(
                                testSeparation,
                                testTranscription,
                                testSubtitles
                        )
                );
            }
        }
    }

    @Test
    void generateKaraokeVideo_shouldCleanUpAssFileOnFailure() throws Exception {
        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            // Setup path mocks
            Path mockAssPath = mock(Path.class);
            when(Paths.get(anyString())).thenReturn(mock(Path.class));
            when(Paths.get("/tmp/" + trackId + ".ass")).thenReturn(mockAssPath);

            // Mock Process to throw during execution
            Process mockProcess = mock(Process.class);
            when(mockProcess.waitFor()).thenThrow(new InterruptedException());

            try (MockedStatic<ProcessBuilder> pbMock = mockStatic(ProcessBuilder.class)) {
                ProcessBuilder mockPb = mock(ProcessBuilder.class);
                pbMock.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
                when(mockPb.start()).thenReturn(mockProcess);

                assertThrows(IOException.class, () ->
                        ffmpegVideoService.generateKaraokeVideo(
                                testSeparation,
                                testTranscription,
                                testSubtitles
                        )
                );

                // Verify cleanup happened
                filesMock.verify(() -> Files.deleteIfExists(mockAssPath));
            }
        }
    }

    @Test
    void generateKaraokeVideo_shouldHandleDirectoryCreationFailure() throws Exception {
        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            // Setup path mocks
            Path mockOutputPath = mock(Path.class);
            Path mockParentDir = mock(Path.class);
            when(Paths.get(anyString())).thenReturn(mock(Path.class));
            when(Paths.get("/output/" + trackId + "/karaoke.mp4")).thenReturn(mockOutputPath);
            when(mockOutputPath.getParent()).thenReturn(mockParentDir);

            // Make directory creation fail
            filesMock.when(() -> Files.createDirectories(mockParentDir))
                    .thenThrow(new IOException("Failed to create directory"));

            assertThrows(IOException.class, () ->
                    ffmpegVideoService.generateKaraokeVideo(
                            testSeparation,
                            testTranscription,
                            testSubtitles
                    )
            );
        }
    }
}