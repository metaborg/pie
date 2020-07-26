package mb.pie.api.exec;

/**
 * Cancellation token implementation that never cancels.
 */
public final class NullCancelableToken implements Cancelable, CancelToken {
    public static final NullCancelableToken instance = new NullCancelableToken();

    private NullCancelableToken() {}

    @Override public void requestCancel() {}

    @Override public boolean isCanceled() {
        return false;
    }
}
