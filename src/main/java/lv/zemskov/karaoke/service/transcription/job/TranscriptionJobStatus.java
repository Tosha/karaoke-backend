package lv.zemskov.karaoke.service.transcription.job;

import lv.zemskov.karaoke.model.TranscriptionResult;

public record TranscriptionJobStatus(TranscriptionJobState state, TranscriptionResult result) {
}