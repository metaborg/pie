package mb.pie.api.exec;

/**
 * Cancelled implementation that never cancels.
 */
public final class NullCancelToken implements CancelToken {

    private static NullCancelToken instance = new NullCancelToken();
    /**
     * Gets the singleton instance of this class.
     * @return The instance.
     */
    public static NullCancelToken getInstance() { return instance; }

    private NullCancelToken() { }

    @Override public boolean isCanceled() {
        return false;
    }
}
