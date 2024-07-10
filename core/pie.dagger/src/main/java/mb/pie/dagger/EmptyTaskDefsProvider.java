package mb.pie.dagger;

import mb.pie.api.TaskDef;

import java.util.Collections;
import java.util.Set;

public class EmptyTaskDefsProvider implements TaskDefsProvider {
    @Override public Set<TaskDef<?, ?>> getTaskDefs() {
        return Collections.emptySet();
    }
}
