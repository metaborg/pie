package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Collection of task definitions.
 */
public interface TaskDefs {
    /**
     * Gets task definition for given ID.
     *
     * @param id ID of the task definition.
     * @return Task definition for given ID, or null if it was not found.
     */
    @Nullable TaskDef<?, ?> getTaskDef(String id);

    /**
     * Gets task definition for given ID, and casts it to the requested type. Using the task definition with an
     * incorrect type will cause {@link ClassCastException}s when executing tasks of that task definition.
     *
     * @param id  ID of the task definition.
     * @param <I> Task definition input type.
     * @param <O> Task definition output type.
     * @return Task definition for given ID, or null if it was not found.
     */
    @Nullable <I extends Serializable, O extends @Nullable Serializable> TaskDef<I, O> getCastedTaskDef(String id);
}
