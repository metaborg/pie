package mb.pie.lang.test.controlFlow.listComprehension.emptyLitToPaths;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.controlFlow.listComprehension.emptyLitToPaths.main_emptyLitToPaths;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_emptyLitToPaths emptyLitToPaths
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(emptyLitToPaths);
        return taskDefs;
    }
}
