package mb.pie.dagger;

import mb.pie.api.TaskDef;

import java.util.Set;

@FunctionalInterface
public interface TaskDefsProvider {
    Set<TaskDef<?, ?>> getTaskDefs();

    default void addTaskDefsTo(PieModule pieModule) {
        pieModule.addTaskDefsFrom(this);
    }

    default void addTaskDefsTo(RootPieModule pieModule) {
        pieModule.addTaskDefsFrom(this);
    }
}
