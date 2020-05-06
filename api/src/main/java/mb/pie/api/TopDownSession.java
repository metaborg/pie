package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Second stage of {@link MixedSession}, after running a bottom-up build with resource changes. This second stage is used
 * to get outputs of existing tasks or to execute new tasks using top-down builds..
 */
public interface TopDownSession extends Session {
    /**
     * Gets the up-to-date output of an existing {@code task}. This method throws an exception when used on tasks that
     * have not been executed before (i.e, a new task or different input). For new tasks, use {@link #require} or {@link
     * #requireWithoutObserving} instead.
     *
     * @param task Task to get result for.
     * @return Up-to-date output of {@code task}.
     * @throws IllegalStateException When {@code task} has not been executed before or when its input object differs
     *                               from an existing task with the same {@link TaskKey key}.
     */
    <O extends @Nullable Serializable> O getOutput(Task<O> task);
}
