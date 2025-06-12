package lv.zemskov.karaoke.service.transcription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lv.zemskov.karaoke.model.*;
import lv.zemskov.karaoke.repository.*;
import lv.zemskov.karaoke.service.job.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class WhisperService {
    private static final Logger log = LoggerFactory.getLogger(WhisperService.class);

    private final SeparationResultRepository separationResultRepository;
    private final TranscriptionResultRepository transcriptionResultRepository;
    private final JobStore<UUID> jobStore;
    private final ObjectMapper objectMapper;

    public WhisperService(
            SeparationResultRepository separationResultRepository,
            TranscriptionResultRepository transcriptionResultRepository,
            JobStore<UUID> jobStore,
            ObjectMapper objectMapper
    ) {
        this.separationResultRepository = separationResultRepository;
        this.transcriptionResultRepository = transcriptionResultRepository;
        this.jobStore = jobStore;
        this.objectMapper = objectMapper;
    }

    @Async
    public void transcribeVocalsAsync(UUID trackId, UUID jobId) {
        try {
            jobStore.update(jobId, JobState.TRANSCRIBING, "Starting audio transcription");

            TranscriptionResult result = transcribeVocals(trackId);
            transcriptionResultRepository.save(result);

            jobStore.complete(jobId, Map.of(
                    "transcriptionId", result.getId(),
                    "message", "Transcription completed successfully"
            ));

        } catch (Exception e) {
            log.error("Transcription failed for track {}: {}", trackId, e.getMessage());
            jobStore.fail(jobId, "Transcription error: " + e.getMessage());
        }
    }

    public TranscriptionResult transcribeVocals(UUID trackId) throws IOException, InterruptedException {
        SeparationResult separation = separationResultRepository.findByTrackId(trackId);

        Path vocalsPath = Paths.get(separation.getVocalsPath());
        log.info("Processing vocals file: {}", vocalsPath);

        ProcessBuilder pb = createWhisperProcess(vocalsPath);
        Process process = pb.start();

        try {
            String output = captureProcessOutput(process);
            if (!process.waitFor(5, TimeUnit.MINUTES)) {
                throw new TimeoutException("Transcription timed out");
            }
            if (process.exitValue() != 0) {
                throw new IOException("Whisper failed: " + output);
            }

            return parseTranscriptionResult(vocalsPath, separation.getTrack());
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            process.destroyForcibly();
        }
    }

    private ProcessBuilder createWhisperProcess(Path audioFile) {
        return new ProcessBuilder(
                "whisper",
                audioFile.toString(),
                "--model", "tiny",
                "--output_dir", "/tmp",
                "--output_format", "json",
                "--word_timestamps", "True",
                "--language", "en",
                "--threads", "4"
        ).redirectErrorStream(true);
    }

    private String captureProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private TranscriptionResult parseTranscriptionResult(Path audioFile, Track track)
            throws IOException {

        Path jsonPath = Paths.get("/tmp", audioFile.getFileName() + ".json");
        JsonNode rootNode = objectMapper.readTree(jsonPath.toFile());

        TranscriptionResult result = new TranscriptionResult();
        result.setTrack(track);
        result.setRawWhisperOutput(objectMapper.writeValueAsString(rootNode));
        result.setLanguageCode(rootNode.path("language").asText());
        result.setConfidenceScore((float) rootNode.path("confidence").asDouble());

        List<LyricsSegment> segments = new ArrayList<>();
        for (JsonNode segment : rootNode.path("segments")) {
            LyricsSegment ls = new LyricsSegment();
            ls.setText(segment.path("text").asText());
            ls.setStartTimeMs((long)(segment.path("start").asDouble() * 1000));
            ls.setEndTimeMs((long)(segment.path("end").asDouble() * 1000));
            ls.setConfidence((float) segment.path("confidence").asDouble());
            ls.setTranscription(result);
            segments.add(ls);
        }
        result.setSegments(segments);

        Files.deleteIfExists(jsonPath);
        return result;
    }

    public TranscriptionResult findByTrackId(UUID trackId) {
        return transcriptionResultRepository.findByTrackId(trackId);
    }
}