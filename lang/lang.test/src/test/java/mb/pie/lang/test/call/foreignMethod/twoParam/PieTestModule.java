package mb.pie.lang.test.call.foreignMethod.twoParam;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.foreignMethod.twoParam.main_twoParam;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_twoParam twoParam
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(twoParam);
        return taskDefs;
    }
}
