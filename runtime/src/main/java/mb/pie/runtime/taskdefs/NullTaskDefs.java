package mb.pie.runtime.taskdefs;

import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NullTaskDefs implements TaskDefs {
    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        return null;
    }
}
