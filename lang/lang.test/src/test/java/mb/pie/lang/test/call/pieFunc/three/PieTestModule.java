package mb.pie.lang.test.call.pieFunc.three;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.pieFunc.empty.helper_empty;
import mb.pie.lang.test.call.pieFunc.empty.main_empty;
import mb.pie.lang.test.call.pieFunc.three.main_three;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_three main,
        helper_three helper
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(main);
        taskDefs.add(helper);
        return taskDefs;
    }
}
