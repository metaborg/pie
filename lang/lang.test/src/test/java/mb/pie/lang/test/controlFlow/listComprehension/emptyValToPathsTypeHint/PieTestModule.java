package mb.pie.lang.test.controlFlow.listComprehension.emptyValToPathsTypeHint;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @Singleton @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_emptyValToPathsTypeHint emptyValToPathsTypeHint
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(emptyValToPathsTypeHint);
        return taskDefs;
    }
}
