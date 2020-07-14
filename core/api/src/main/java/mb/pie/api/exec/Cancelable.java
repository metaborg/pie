package mb.pie.api.exec;

/**
 * Interface for requesting cancellation.
 */
public interface Cancelable {
    /**
     * Request cancellation.
     */
    void requestCancel();
}
