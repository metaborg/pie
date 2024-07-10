package mb.pie.lang.test.call.foreignTask.singleParam;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.foreignTask.singleParam.main_singleParam;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        SingleParam singleParam,
        main_singleParam main_singleParam
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(main_singleParam);
        taskDefs.add(singleParam);
        return taskDefs;
    }
}
