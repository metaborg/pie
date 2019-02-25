package mb.pie.api;

/**
 * Exception that can occur during task execution.
 */
public class ExecException extends Exception {
    ExecException() {
        super();
    }

    ExecException(String message) {
        super(message);
    }

    ExecException(String message, Throwable cause) {
        super(message, cause);
    }

    ExecException(Throwable cause) {
        super(cause);
    }
}
