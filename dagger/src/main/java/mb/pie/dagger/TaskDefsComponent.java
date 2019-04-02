package mb.pie.dagger;

import mb.pie.api.TaskDef;

import java.util.Set;

public interface TaskDefsComponent {
    Set<TaskDef<?, ?>> getTaskDefs();
}
