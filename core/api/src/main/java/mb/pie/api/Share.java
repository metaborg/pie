package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Supplier;

/**
 * Share for concurrently executing tasks.
 */
public interface Share {
    TaskData share(TaskKey key, Supplier<TaskData> execFunc, @Nullable Supplier<TaskData> visitedFunc);
}
