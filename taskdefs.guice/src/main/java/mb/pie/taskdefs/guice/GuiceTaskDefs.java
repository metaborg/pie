package mb.pie.taskdefs.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * [TaskDefs] implementation that injects the taskDefs binding created from [TaskDefsModule].
 */
public class GuiceTaskDefs implements TaskDefs {
    /**
     * Sets the task definitions of this builder to the [GuiceTaskDefs] retrieved from given [injector].
     */
    public static PieBuilder withGuiceTaskDefs(PieBuilder pieBuilder, Injector injector) {
        final GuiceTaskDefs taskDefs = injector.getInstance(GuiceTaskDefs.class);
        pieBuilder.withTaskDefs(taskDefs);
        return pieBuilder;
    }

    /**
     * Sets the task definitions of this builder to the given [taskDefs].
     */
    public static PieBuilder withGuiceTaskDefs(PieBuilder pieBuilder, GuiceTaskDefs taskDefs) {
        pieBuilder.withTaskDefs(taskDefs);
        return pieBuilder;
    }


    private final HashMap<String, TaskDef<?, ?>> taskDefs;

    @Inject public GuiceTaskDefs(HashMap<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = taskDefs;
    }

    @Override
    public @Nullable <I extends Serializable, O extends @Nullable Serializable> TaskDef<I, O> getTaskDef(String id) {
        @SuppressWarnings("unchecked") final @Nullable TaskDef<I, O> taskDef = (@Nullable TaskDef<I, O>) taskDefs.get(id);
        return taskDef;
    }
}
