package mb.pie.api.exec;

/**
 * Thread interrupt cancellation token implementation.
 */
public class InterruptCancelToken implements Cancel, Cancelled {
    private Thread thread = Thread.currentThread();

    @Override public void requestCancel() {
        thread.interrupt();
    }

    @Override public boolean isCancelled() {
        return thread.isInterrupted();
    }

    @Override public void throwIfCancelled() throws InterruptedException {
        if(isCancelled()) {
            throw new InterruptedException();
        }
    }
}
