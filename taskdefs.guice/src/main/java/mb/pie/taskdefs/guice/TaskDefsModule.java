package mb.pie.taskdefs.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import mb.pie.api.TaskDef;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/**
 * A module for binding task definitions. Extend this module, and implement the [bindTaskDefs] function by making calls to [bindTaskDef] to
 * bind task definitions.
 */
public abstract class TaskDefsModule extends AbstractModule {
    abstract void bindTaskDefs();

    protected <B extends TaskDef<?, ?>> void bindTaskDef(Class<B> clazz, String id) {
        bind(clazz).in(Singleton.class);
        taskDefsBinder.addBinding(id).to(clazz);
    }


    @SuppressWarnings("NullableProblems") @MonotonicNonNull private MapBinder<String, TaskDef<?, ?>> taskDefsBinder;

    @Override public void configure() {
        // @formatter:off
        taskDefsBinder = MapBinder.newMapBinder(binder(), new TypeLiteral<String>() {}, new TypeLiteral<TaskDef<?, ?>>() {});
        // @formatter:on

        bindTaskDefs();
    }
}
