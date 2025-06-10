package lv.zemskov.karaoke.service.transcription.job;

import lv.zemskov.karaoke.model.TranscriptionResult;

public class TranscriptionJobStatus {
    private final TranscriptionJobState state;
    private final TranscriptionResult result;

    public TranscriptionJobStatus(TranscriptionJobState state, TranscriptionResult result) {
        this.state = state;
        this.result = result;
    }

    public TranscriptionJobState getState() {
        return state;
    }

    public TranscriptionResult getResult() {
        return result;
    }
}