package mb.pie.dagger;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.TaskDef;
import mb.resource.dagger.ResourceServiceComponent;

import java.util.Set;

@PieScope
@Component(
    modules = {
        PieModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class
    }
)
public interface PieComponent extends AutoCloseable {
    Pie getPie();


    default MixedSession newSession() {
        return getPie().newSession();
    }


    default PieModule createChildModule() {
        return new PieModule(getPie());
    }

    default PieModule createChildModule(Set<TaskDef<?, ?>> taskDefs) {
        return new PieModule(getPie(), taskDefs);
    }

    default PieModule createChildModule(TaskDef<?, ?>... taskDefs) {
        return new PieModule(getPie(), taskDefs);
    }

    default PieModule createChildModule(TaskDefsProvider taskDefsComponent) {
        return new PieModule(getPie(), taskDefsComponent);
    }


    @Override default void close() {
        getPie().close();
    }
}
