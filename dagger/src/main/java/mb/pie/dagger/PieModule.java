package mb.pie.dagger;

import dagger.Module;
import dagger.Provides;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.TaskDef;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.taskdefs.MapTaskDefs;

import java.util.Set;

@Module
public class PieModule {
    @Provides Pie providePie(Set<TaskDef<?, ?>> taskDefs) {
        final PieBuilder builder = new PieBuilderImpl();
        builder.withTaskDefs(new MapTaskDefs(taskDefs));
        return builder.build();
    }
}
