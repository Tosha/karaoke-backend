package lv.zemskov.karaoke.exception;

public class JobProcessingException extends RuntimeException {
    public JobProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}