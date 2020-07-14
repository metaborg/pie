package mb.pie.lang.test.binary.neq.neqIntDifferent;

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
        main_neqIntDifferent neqIntDifferent
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(neqIntDifferent);
        return taskDefs;
    }
}
