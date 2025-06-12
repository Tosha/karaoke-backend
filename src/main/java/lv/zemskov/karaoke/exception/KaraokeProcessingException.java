package lv.zemskov.karaoke.exception;

public class KaraokeProcessingException extends RuntimeException {
    public KaraokeProcessingException(String message, Exception e) {
        super(message, e);
    }
}