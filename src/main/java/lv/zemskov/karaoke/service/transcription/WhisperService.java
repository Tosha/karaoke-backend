package lv.zemskov.karaoke.service.transcription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lv.zemskov.karaoke.model.LyricsSegment;
import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.repository.TranscriptionResultRepository;
import lv.zemskov.karaoke.service.transcription.job.TranscriptionJobStatus;
import lv.zemskov.karaoke.service.transcription.job.TranscriptionJobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class WhisperService {

    private static final Logger log = LoggerFactory.getLogger(WhisperService.class);

    private final SeparationResultRepository separationResultRepository;
    private final TranscriptionResultRepository transcriptionResultRepository;
    private final TranscriptionJobStore jobStore;

    public WhisperService(
            SeparationResultRepository separationResultRepository,
            TranscriptionResultRepository transcriptionResultRepository,
            TranscriptionJobStore jobStore
    ) {
        this.separationResultRepository = separationResultRepository;
        this.transcriptionResultRepository = transcriptionResultRepository;
        this.jobStore = jobStore;
    }

    @Async
    public void transcribeVocalsAsync(UUID trackId, UUID jobId) {
        try {
            jobStore.setInProgress(jobId);
            TranscriptionResult result = transcribeVocals(trackId);
            jobStore.setDone(jobId, result);
        } catch (Exception e) {
            log.error("Transcription failed for track {}: {}", trackId, e.getMessage());
            jobStore.setError(jobId);
        }
    }

    public TranscriptionResult transcribeVocals(UUID trackId) throws IOException, InterruptedException {
        SeparationResult separation = separationResultRepository.findByTrackId(trackId);

        Path vocalsPath = Paths.get(separation.getVocalsPath());
        log.info("Path for vocals found: {}", vocalsPath);

        ProcessBuilder pb = new ProcessBuilder(
                "whisper",
                vocalsPath.toString(),
                "--model", "tiny",
                "--output_dir", "/tmp",
                "--output_format", "json",
                "--word_timestamps", "True",
                "--language", "en",
                "--beam_size", "1",
                "--best_of", "1",
                "--threads", "4",
                "--compression_ratio_threshold", "2.4",
                "--fp16", "False"
        ).redirectErrorStream(true);

        Process process = pb.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));
        log.info("Whisper CLI raw output:\n{}", output);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Whisper failed. Output:\n{}", output);
            throw new RuntimeException("Whisper failed with code: " + exitCode + "\n" + output);
        }

        Path jsonOutput = Paths.get("/tmp", vocalsPath.getFileName() + ".json");
        String jsonContent = Files.readString(jsonOutput);
        JsonNode rootNode = new ObjectMapper().readTree(jsonContent);

        TranscriptionResult result = new TranscriptionResult();
        result.setTrack(separation.getTrack());
        result.setRawWhisperOutput(jsonContent);
        result.setLanguageCode(rootNode.path("language").asText());
        result.setConfidenceScore((float) rootNode.path("confidence").asDouble());

        List<LyricsSegment> segments = new ArrayList<>();
        for (JsonNode segment : rootNode.path("segments")) {
            LyricsSegment ls = new LyricsSegment();
            ls.setText(segment.path("text").asText());
            ls.setStartTimeMs((long) (segment.path("start").asDouble() * 1000));
            ls.setEndTimeMs((long) (segment.path("end").asDouble() * 1000));
            ls.setConfidence((float) segment.path("confidence").asDouble());
            ls.setTranscription(result);
            segments.add(ls);
        }

        result.setSegments(segments);
        return transcriptionResultRepository.save(result);
    }

    public TranscriptionResult findByTrackId(Long trackId) {
        return transcriptionResultRepository.findById(trackId).orElseThrow();
    }

    public TranscriptionJobStatus getJobStatus(UUID jobId) {
        return jobStore.getStatus(jobId);
    }
}