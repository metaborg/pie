package mb.pie.api.exec;

/**
 * Cancelled implementation that never cancels.
 */
public class NullCancelled implements Cancelled {
    @Override public boolean isCancelled() {
        return false;
    }

    @Override public void throwIfCancelled() {

    }
}
