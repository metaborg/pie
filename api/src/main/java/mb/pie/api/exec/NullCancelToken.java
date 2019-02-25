package mb.pie.api.exec;

/**
 * Cancellation token implementation that never cancels.
 */
public class NullCancelToken implements Cancel, Cancelled {
    @Override public void requestCancel() {}

    @Override public boolean isCancelled() {
        return false;
    }

    @Override public void throwIfCancelled() {}
}
