package lv.zemskov.karaoke.exception;

public class InvalidAudioFileException extends RuntimeException {
    public InvalidAudioFileException(String message) {
        super(message);
    }
}