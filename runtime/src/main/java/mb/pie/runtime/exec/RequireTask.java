package mb.pie.runtime.exec;

import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.pie.api.exec.Cancelled;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface RequireTask {
    <I extends Serializable, O extends @Nullable Serializable> O require(TaskKey key, Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException;
}
