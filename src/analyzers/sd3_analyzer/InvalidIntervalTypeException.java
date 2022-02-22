package analyzers.sd3_analyzer;

public class InvalidIntervalTypeException extends Exception {
    private static final long serialVersionUID = 7_718_828_512_143_293_558L;

    public InvalidIntervalTypeException() {}

    public InvalidIntervalTypeException(String message) {
        super(message);
    }

    public InvalidIntervalTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidIntervalTypeException(Throwable cause) {
        super(cause);
    }
}
