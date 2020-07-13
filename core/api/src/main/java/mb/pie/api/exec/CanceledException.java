package mb.pie.api.exec;

public class CanceledException extends RuntimeException {
    public CanceledException() {
        super();
    }

    public CanceledException(String message) {
        super(message);
    }


    public InterruptedException toInterruptedException() {
        final InterruptedException interruptedException = new InterruptedException(this.getMessage());
        interruptedException.setStackTrace(this.getStackTrace());
        return interruptedException;
    }
}
