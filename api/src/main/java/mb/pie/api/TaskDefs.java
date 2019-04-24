package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

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
}
