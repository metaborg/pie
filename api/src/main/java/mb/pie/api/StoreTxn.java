package mb.pie.api;

/**
 * Storage transaction. Must be closed after use.
 */
public interface StoreTxn extends AutoCloseable {
    /**
     * Closes the transaction. Commits written data and frees up internal resources. Failure to close a transaction may
     * cause memory leaks and written data to not be visible to other transactions.
     */
    @Override void close();
}
