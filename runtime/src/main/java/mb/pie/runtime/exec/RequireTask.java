package mb.pie.runtime.exec;

import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.pie.api.exec.Cancelled;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public interface RequireTask {
    <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, boolean modifyObservability, Cancelled cancel) throws ExecException, InterruptedException;
}
