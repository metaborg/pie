package mb.pie.api.exec;

/**
 * Cancellation token implementation that never cancels.
 */
public final class NullCancelableToken implements Cancelable, CancelToken {

    /** The singleton instance of this class. */
    public static final NullCancelableToken instance = new NullCancelableToken();

    private NullCancelableToken() {}

    @Override
    public void requestCancel() {
        // Nothing to do.
    }

    @Override public boolean isCanceled() {
        return false;
    }

}
