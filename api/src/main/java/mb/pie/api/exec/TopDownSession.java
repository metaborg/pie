package mb.pie.api.exec;

import mb.pie.api.ExecException;
import mb.pie.api.Task;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface TopDownSession {
    /**
     * Requires given [task], returning its output.
     */
    <O extends @Nullable Serializable> O requireInitial(Task<O> task) throws ExecException;

    /**
     * Requires given [task], with given [cancel] requester, returning its output.
     */
    <O extends @Nullable Serializable> O requireInitial(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException;
}
