package analyzers.baseline_analyzer;


public class NullLoopInstanceException extends Exception {

    private static final long serialVersionUID = 7_718_828_512_143_293_558L;

    public NullLoopInstanceException() {}

    public NullLoopInstanceException(String message) {
        super(message);
    }

    public NullLoopInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullLoopInstanceException(Throwable cause) {
        super(cause);
    }

}
