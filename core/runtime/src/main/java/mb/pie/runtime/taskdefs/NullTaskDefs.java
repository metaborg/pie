package mb.pie.runtime.taskdefs;

import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;

public class NullTaskDefs implements TaskDefs {
    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) { return null; }

    @Override public Iterable<TaskDef<?, ?>> getAllTaskDefs() {
        return Collections.emptySet();
    }

    @Override public boolean exists(String id) { return false; }
}
