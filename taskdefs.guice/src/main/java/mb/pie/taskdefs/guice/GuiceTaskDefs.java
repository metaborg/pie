package mb.pie.taskdefs.guice;

import com.google.inject.Inject;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Map;

/**
 * [TaskDefs] implementation that injects the taskDefs binding created from [TaskDefsModule].
 */
public class GuiceTaskDefs implements TaskDefs {
    private final Map<String, TaskDef<?, ?>> taskDefs;

    @Inject public GuiceTaskDefs(Map<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = taskDefs;
    }

    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        return taskDefs.get(id);
    }

    @Override
    public <I extends Serializable, O extends Serializable> @Nullable TaskDef<I, O> getCastedTaskDef(String id) {
        @SuppressWarnings("unchecked") final @Nullable TaskDef<I, O> taskDef =
            (@Nullable TaskDef<I, O>) taskDefs.get(id);
        return taskDef;
    }
}
