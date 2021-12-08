package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Set;

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
     * Gets all task definitions.
     */
    Iterable<TaskDef<?, ?>> getAllTaskDefs();

    /**
     * Checks whether task definition with given ID exists.
     *
     * @param id ID of the task definition.
     * @return True if it exists, false if not.
     */
    boolean exists(String id);
}
