package lv.zemskov.karaoke.service.separation;

import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.Track;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.repository.TrackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpleeterServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private SeparationResultRepository separationResultRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private SpleeterService spleeterService;

    @Test
    void processAudio_shouldSuccessfullyProcessAudio() throws Exception {
        // Setup test data
        Track savedTrack = new Track();
        UUID trackId = UUID.randomUUID();
        savedTrack.setId(trackId);
        savedTrack.setStatus("PROCESSING");

        SeparationResult expectedResult = new SeparationResult();
        expectedResult.setTrack(savedTrack);

        // Mock repository behavior
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);
        when(separationResultRepository.save(any(SeparationResult.class))).thenReturn(expectedResult);

        // Mock file operations
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp3");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Mock static Files and Paths methods
        try (var filesMock = mockStatic(Files.class);
             var pathsMock = mockStatic(Paths.class)) {

            // Mock Paths
            Path mockInputDir = mock(Path.class);
            Path mockOutputDir = mock(Path.class);
            Path mockInputFile = mock(Path.class);
            Path mockOutputSubdir = mock(Path.class);
            Path mockVocalsFile = mock(Path.class);
            Path mockAccompanimentFile = mock(Path.class);

            when(Paths.get("/input")).thenReturn(mockInputDir);
            when(Paths.get("/output")).thenReturn(mockOutputDir);
            when(Paths.get("/input/" + trackId + ".mp3")).thenReturn(mockInputFile);
            when(Paths.get("/output/" + trackId)).thenReturn(mockOutputSubdir);
            when(Paths.get("/output/" + trackId + "/vocals.wav")).thenReturn(mockVocalsFile);
            when(Paths.get("/output/" + trackId + "/accompaniment.wav")).thenReturn(mockAccompanimentFile);

            // Mock Files
            filesMock.when(() -> Files.createDirectories(mockInputDir)).thenReturn(mockInputDir);
            filesMock.when(() -> Files.createDirectories(mockOutputDir)).thenReturn(mockOutputDir);
            filesMock.when(() -> Files.createDirectories(mockOutputSubdir)).thenReturn(mockOutputSubdir);

            // Mock process execution
            Process mockProcess = mock(Process.class);
            when(mockProcess.waitFor()).thenReturn(0);

            try (var pbMock = mockStatic(ProcessBuilder.class)) {
                ProcessBuilder mockPb = mock(ProcessBuilder.class);
                pbMock.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
                when(mockPb.inheritIO()).thenReturn(mockPb);
                when(mockPb.start()).thenReturn(mockProcess);

                // Execute test
                SeparationResult result = spleeterService.processAudio(multipartFile);

                // Verify results
                assertNotNull(result);
                assertEquals(savedTrack, result.getTrack());
                verify(trackRepository, times(3)).save(any(Track.class));
                verify(separationResultRepository).save(any(SeparationResult.class));
            }
        }
    }

    @Test
    void processAudio_shouldHandleProcessFailure() throws Exception {
        // Setup test data
        Track savedTrack = new Track();
        UUID trackId = UUID.randomUUID();
        savedTrack.setId(trackId);
        savedTrack.setStatus("PROCESSING");

        // Mock repository behavior
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        // Mock file operations
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp3");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Mock static Files and Paths methods
        try (var filesMock = mockStatic(Files.class);
             var pathsMock = mockStatic(Paths.class)) {

            // Mock Paths
            Path mockInputFile = mock(Path.class);
            when(Paths.get(anyString())).thenReturn(mock(Path.class));
            when(Paths.get("/input/" + trackId + ".mp3")).thenReturn(mockInputFile);

            // Mock process execution to fail
            Process mockProcess = mock(Process.class);
            when(mockProcess.waitFor()).thenReturn(1); // Non-zero exit code

            try (var pbMock = mockStatic(ProcessBuilder.class)) {
                ProcessBuilder mockPb = mock(ProcessBuilder.class);
                pbMock.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
                when(mockPb.inheritIO()).thenReturn(mockPb);
                when(mockPb.start()).thenReturn(mockProcess);

                // Execute and verify exception
                assertThrows(RuntimeException.class, () ->
                        spleeterService.processAudio(multipartFile)
                );

                // Verify track status was updated to FAILED
                verify(trackRepository, times(2)).save(argThat(track ->
                        track.getStatus().equals("FAILED")
                ));
            }
        }
    }

    @Test
    void processAudio_shouldHandleIOException() throws Exception {
        // Setup test data
        Track savedTrack = new Track();
        savedTrack.setId(UUID.randomUUID());
        savedTrack.setStatus("PROCESSING");

        // Mock repository behavior
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        // Mock file operations to throw exception
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp3");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Execute and verify
        assertThrows(IOException.class, () ->
                spleeterService.processAudio(multipartFile)
        );

        // Verify track status was updated to FAILED
        verify(trackRepository, times(2)).save(argThat(track ->
                track.getStatus().equals("FAILED")
        ));
    }

    @Test
    void processAudio_shouldHandleInterruptedException() throws Exception {
        // Setup test data
        Track savedTrack = new Track();
        UUID trackId = UUID.randomUUID();
        savedTrack.setId(trackId);
        savedTrack.setStatus("PROCESSING");

        // Mock repository behavior
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        // Mock file operations
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp3");
        when(multipartFile.getSize()).thenReturn(1024L);

        // Mock static Files and Paths methods
        try (var filesMock = mockStatic(Files.class);
             var pathsMock = mockStatic(Paths.class)) {

            // Mock Paths
            Path mockInputFile = mock(Path.class);
            when(Paths.get(anyString())).thenReturn(mock(Path.class));
            when(Paths.get("/input/" + trackId + ".mp3")).thenReturn(mockInputFile);

            // Mock process to throw InterruptedException
            Process mockProcess = mock(Process.class);
            when(mockProcess.waitFor()).thenThrow(new InterruptedException("Test interrupt"));

            try (var pbMock = mockStatic(ProcessBuilder.class)) {
                ProcessBuilder mockPb = mock(ProcessBuilder.class);
                pbMock.when(() -> new ProcessBuilder(any(String[].class))).thenReturn(mockPb);
                when(mockPb.inheritIO()).thenReturn(mockPb);
                when(mockPb.start()).thenReturn(mockProcess);

                // Execute and verify
                assertThrows(IOException.class, () ->
                        spleeterService.processAudio(multipartFile)
                );

                // Verify track status was updated to FAILED
                verify(trackRepository, times(2)).save(argThat(track ->
                        track.getStatus().equals("FAILED")
                ));
            }
        }
    }
}