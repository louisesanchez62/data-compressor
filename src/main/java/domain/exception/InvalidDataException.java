package domain.exception;

public class InvalidDataException extends CompressionException {
    public InvalidDataException(String message) {
        super(message);
    }
}
