package mb.pie.api.exec;

/**
 * Interface for checking if an operation has been canceled.
 */
public interface CancelToken {
    /**
     * Gets whether the operation has been canceled.
     *
     * @return {@code true} when cancellation has been requested; otherwise, {@code false}.
     */
    boolean isCanceled();

    /**
     * Throws an InterruptedException when the operation has been canceled.
     *
     * @throws CanceledException When cancellation has been requested.
     */
    default void throwIfCanceled() {
        if(isCanceled()) {
            throw new CanceledException();
        }
    }
}
