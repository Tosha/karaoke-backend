package lv.zemskov.karaoke.service.job;

public enum JobState {
    CREATED,
    UPLOADING,
    SEPARATING,
    TRANSCRIBING,
    GENERATING_SUBTITLES,
    RENDERING,
    COMPLETED,
    FAILED
}
