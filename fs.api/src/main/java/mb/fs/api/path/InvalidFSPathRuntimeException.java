package mb.fs.api.path;

public class InvalidFSPathRuntimeException extends RuntimeException {
    public InvalidFSPathRuntimeException() {
        super();
    }

    public InvalidFSPathRuntimeException(String message) {
        super(message);
    }

    public InvalidFSPathRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFSPathRuntimeException(Throwable cause) {
        super(cause);
    }
}
