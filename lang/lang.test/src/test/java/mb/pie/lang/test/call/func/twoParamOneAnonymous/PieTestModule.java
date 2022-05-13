package mb.pie.lang.test.call.func.twoParamOneAnonymous;

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
        main_twoParamOneAnonymous twoParamOneAnonymous,
        helper_twoParamOneAnonymous helper
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 2);
        taskDefs.add(twoParamOneAnonymous);
        taskDefs.add(helper);
        return taskDefs;
    }
}
