package mb.pie.api.exec;

/**
 * Thread interrupt cancellation token implementation.
 */
public final class InterruptCancelableToken implements Cancelable, CancelToken {
    private Thread thread = Thread.currentThread();

    @Override public void requestCancel() {
        thread.interrupt();
    }

    @Override public boolean isCanceled() {
        return thread.isInterrupted();
    }
}
