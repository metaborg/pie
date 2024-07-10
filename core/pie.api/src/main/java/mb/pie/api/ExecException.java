package mb.pie.api;

/**
 * Exception that can occur during task execution.
 */
public class ExecException extends Exception {
    public ExecException() {
        super();
    }

    public ExecException(String message) {
        super(message);
    }

    public ExecException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecException(Throwable cause) {
        super(cause);
    }
}
