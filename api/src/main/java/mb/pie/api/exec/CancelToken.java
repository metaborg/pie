package mb.pie.api.exec;

/**
 * Simple cancellation token implementation.
 */
public class CancelToken implements Cancel, Cancelled {
    private volatile boolean cancel = false;

    @Override public void requestCancel() {
        cancel = true;
    }

    @Override public boolean isCancelled() {
        return cancel;
    }

    @Override public void throwIfCancelled() throws InterruptedException {
        if(cancel) {
            throw new InterruptedException();
        }
    }
}
