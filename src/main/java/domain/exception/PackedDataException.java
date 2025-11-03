package domain.exception;

public class PackedDataException extends RuntimeException {
    public PackedDataException(String message) {
        super(message);
    }

    public PackedDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
