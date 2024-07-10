package mb.pie.api;

/**
 * Exception that can occur during task execution.
 */
public class UncheckedExecException extends RuntimeException {
    public UncheckedExecException(String message, Throwable cause) {
        super(message, cause, true, true); // Disable suppression and stacktraces, as it is just a wrapper.
    }

    public ExecException toChecked() {
        final ExecException execException = new ExecException(this.getMessage(), this.getCause());
        execException.setStackTrace(this.getStackTrace());
        return execException;
    }
}
