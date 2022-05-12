package mb.pie.lang.test.variables.anonymous.multiUseNamed;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_multiUseNamed multiUseNamed
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(multiUseNamed);
        return taskDefs;
    }
}
