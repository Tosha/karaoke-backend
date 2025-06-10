package lv.zemskov.karaoke.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lv.zemskov.karaoke.model.LyricsSegment;
import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.repository.SeparationResultRepository;
import lv.zemskov.karaoke.repository.TranscriptionResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WhisperService {
    private static final Logger logger = LoggerFactory.getLogger(WhisperService.class);

    private final SeparationResultRepository separationResultRepository;
    private final TranscriptionResultRepository transcriptionResultRepository;
    private final ResourceLoader resourceLoader;

    public WhisperService(SeparationResultRepository separationResultRepository,
                                       TranscriptionResultRepository transcriptionResultRepository,
                                       ResourceLoader resourceLoader) {
        this.separationResultRepository = separationResultRepository;
        this.transcriptionResultRepository = transcriptionResultRepository;
        this.resourceLoader = resourceLoader;
    }

    public TranscriptionResult transcribeVocals(UUID trackId) throws IOException, InterruptedException {
        // 1. Get vocal file path
        SeparationResult separation = separationResultRepository.findByTrackId(trackId);

        Path vocalsPath = Paths.get(separation.getVocalsPath());

        // 2. Prepare Whisper command
        ProcessBuilder pb = new ProcessBuilder(
                "whisper",
                vocalsPath.toString(),
                "--model", "medium",
                "--output_dir", "/tmp",
                "--output_format", "json",
                "--word_timestamps", "True",
                "--language", "auto"
        );

        // 3. Execute and parse results
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Whisper failed with code: " + exitCode);
        }

        // 4. Parse JSON output
        Path jsonOutput = Paths.get("/tmp", vocalsPath.getFileName() + ".json");
        String jsonContent = Files.readString(jsonOutput);
        JsonNode rootNode = new ObjectMapper().readTree(jsonContent);

        // 5. Store results
        TranscriptionResult result = new TranscriptionResult();
        result.setTrack(separation.getTrack());
        result.setRawWhisperOutput(jsonContent);
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
        return transcriptionResultRepository.save(result);
    }

    public TranscriptionResult findByTrackId(Long trackId) {
        return transcriptionResultRepository.findById(trackId).orElseThrow();
    }
}