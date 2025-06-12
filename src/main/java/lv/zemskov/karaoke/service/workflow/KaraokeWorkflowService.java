package lv.zemskov.karaoke.service.workflow;

import lv.zemskov.karaoke.exception.KaraokeProcessingException;
import lv.zemskov.karaoke.model.SeparationResult;
import lv.zemskov.karaoke.model.Track;
import lv.zemskov.karaoke.model.TranscriptionResult;
import lv.zemskov.karaoke.service.AudioUploadService;
import lv.zemskov.karaoke.service.job.JobState;
import lv.zemskov.karaoke.service.job.JobStatus;
import lv.zemskov.karaoke.service.job.JobStore;
import lv.zemskov.karaoke.service.separation.SpleeterService;
import lv.zemskov.karaoke.service.subtitle.AssSubtitleService;
import lv.zemskov.karaoke.service.transcription.WhisperService;
import lv.zemskov.karaoke.service.video.FfmpegVideoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

@Service
@Transactional
public class KaraokeWorkflowService {
    private final AudioUploadService uploadService;
    private final SpleeterService spleeterService;
    private final WhisperService whisperService;
    private final AssSubtitleService subtitleService;
    private final FfmpegVideoService videoService;
    private final JobStore<UUID> jobStore;

    public KaraokeWorkflowService(AudioUploadService uploadService,
                                  SpleeterService spleeterService,
                                  WhisperService whisperService,
                                  AssSubtitleService subtitleService,
                                  FfmpegVideoService videoService,
                                  JobStore<UUID> jobStore) {
        this.uploadService = uploadService;
        this.spleeterService = spleeterService;
        this.whisperService = whisperService;
        this.subtitleService = subtitleService;
        this.videoService = videoService;
        this.jobStore = jobStore;
    }

    public UUID processAudio(MultipartFile file) {
        UUID jobId = UUID.randomUUID();
        jobStore.create(jobId, JobState.CREATED, "Initializing processing");

        try {
            // 1. Upload and validate
            jobStore.update(jobId, JobState.UPLOADING, "Uploading audio");
            Track track = uploadService.processUpload(file);

            // 2. Separate vocals
            jobStore.update(jobId, JobState.SEPARATING, "Separating audio tracks");
            SeparationResult separation = spleeterService.processAudio(file);

            // 3. Transcribe vocals
            jobStore.update(jobId, JobState.TRANSCRIBING, "Transcribing vocals");
            TranscriptionResult transcription = whisperService.transcribeVocals(separation.getTrack().getId());

            // 4. Generate subtitles
            jobStore.update(jobId, JobState.GENERATING_SUBTITLES, "Creating karaoke subtitles");
            String assContent = subtitleService.generateAssSubtitles(transcription);

            // 5. Render video
            jobStore.update(jobId, JobState.RENDERING, "Generating karaoke video");
            Path videoPath = videoService.generateKaraokeVideo(separation, transcription, assContent);

            jobStore.complete(jobId, videoPath.toString());
            return jobId;

        } catch (Exception e) {
            jobStore.fail(jobId, e.getMessage());
            throw new KaraokeProcessingException("Failed processing job " + jobId, e);
        }
    }

    public JobStatus<UUID> getJobStatus(UUID jobId) {
        return jobStore.getStatus(jobId);
    }

    public Path getResult(UUID jobId) {
        JobStatus<UUID> status = jobStore.getStatus(jobId);
        if (status.state() != JobState.COMPLETED) {
            throw new IllegalStateException("Job not completed");
        }
        return Path.of((String) status.result());
    }
}