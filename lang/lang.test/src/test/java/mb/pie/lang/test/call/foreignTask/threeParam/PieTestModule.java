package mb.pie.lang.test.call.foreignTask.threeParam;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.foreignTask.threeParam.main_threeParam;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        ThreeParam threeParam,
        main_threeParam main_threeParam
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(threeParam);
        taskDefs.add(main_threeParam);
        return taskDefs;
    }
}
