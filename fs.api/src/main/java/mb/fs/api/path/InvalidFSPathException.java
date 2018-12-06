package mb.fs.api.path;

public class InvalidFSPathException extends Exception {
    public InvalidFSPathException() {
        super();
    }

    public InvalidFSPathException(String message) {
        super(message);
    }

    public InvalidFSPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFSPathException(Throwable cause) {
        super(cause);
    }
}
