package mb.pie.api.exec;

public class UncheckedInterruptedException extends RuntimeException {
    public final InterruptedException interruptedException;

    public UncheckedInterruptedException(InterruptedException interruptedException) {
        super(null, interruptedException, true, true); // Disable suppression and stacktraces, as it is just a wrapper.
        this.interruptedException = interruptedException;
    }
}
