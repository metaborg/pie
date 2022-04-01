package mb.pie.lang.test.call.foreignTask.twoParam;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.foreignTask.twoParam.main_twoParam;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        TwoParam twoParam,
        main_twoParam main_twoParam
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(twoParam);
        taskDefs.add(main_twoParam);
        return taskDefs;
    }
}
