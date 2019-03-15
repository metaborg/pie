package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Collection of task definitions.
 */
public interface TaskDefs {
    @Nullable <I extends Serializable, O extends @Nullable Serializable> TaskDef<I, O> getTaskDef(String id);
}
