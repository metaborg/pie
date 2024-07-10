package mb.pie.api;

/**
 * Internal storage for tasks, outputs, and dependency information.
 */
public interface Store extends AutoCloseable {
    /**
     * Opens a read transaction. Transaction must be [closed][close] after usage to free up internal resources.
     */
    StoreReadTxn readTxn();

    /**
     * Opens a write transaction. Transaction must be [closed][close] after usage to commit written data and to free up
     * internal resources.
     */
    StoreWriteTxn writeTxn();

    /**
     * Force synchronization of in-memory data to persistent storage.
     *
     * @throws RuntimeException when synchronization fails.
     */
    void sync();

    /**
     * Closes the storage, releasing any hold resources.
     *
     * @throws RuntimeException when closing fails.
     */
    @Override void close();
}
