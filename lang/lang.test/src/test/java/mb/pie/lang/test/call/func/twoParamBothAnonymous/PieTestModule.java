package mb.pie.lang.test.call.func.twoParamBothAnonymous;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_twoParamBothAnonymous twoParamBothAnonymous,
        helper_twoParamBothAnonymous helper
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 2);
        taskDefs.add(twoParamBothAnonymous);
        taskDefs.add(helper);
        return taskDefs;
    }
}
