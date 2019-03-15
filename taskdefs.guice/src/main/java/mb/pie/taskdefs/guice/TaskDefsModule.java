package mb.pie.taskdefs.guice;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import mb.pie.api.TaskDef;

/**
 * A module for binding task definitions. Extend this module, and implement the [bindTaskDefs] function by making calls to [bindTaskDef] to
 * bind task definitions.
 */
public abstract class TaskDefsModule implements Module {
    @Override public void configure(Binder binder) {
        final MapBinder<String, TaskDef<?, ?>> taskDefsBinder = MapBinder.newMapBinder(binder, new TypeLiteral<String>() {

        }, new TypeLiteral<TaskDef<?, ?>>() {

        });
        bindTaskDefs(binder, taskDefsBinder);
    }

    abstract void bindTaskDefs(Binder binder, MapBinder<String, TaskDef<?, ?>> taskDefsBinder);

    protected <B extends TaskDef<?, ?>> void bindTaskDef(Class<B> clazz, Binder binder, MapBinder<String, TaskDef<?, ?>> builderBinder, String id) {
        binder.bind(clazz).in(Singleton.class);
        builderBinder.addBinding(id).to(clazz);
    }
}
