package mb.pie.api;

import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.exec.TopDownExecutor;

/**
 * Facade for PIE.
 */
public interface Pie extends AutoCloseable {
    TopDownExecutor getTopDownExecutor();

    BottomUpExecutor getBottomUpExecutor();

    void dropStore();
}
