package domain.exception;

public class DecompressionException extends CompressionException {
    public DecompressionException(String message) {
        super(message);
    }

    public DecompressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
