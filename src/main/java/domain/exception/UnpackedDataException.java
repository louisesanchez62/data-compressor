package domain.exception;

public class UnpackedDataException extends RuntimeException {
    public UnpackedDataException(String message) {
        super(message);
    }

    public UnpackedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
