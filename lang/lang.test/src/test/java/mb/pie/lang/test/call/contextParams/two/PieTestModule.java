package mb.pie.lang.test.call.contextParams.two;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.contextParams.empty.helper_empty;
import mb.pie.lang.test.call.contextParams.empty.main_empty;
import mb.pie.lang.test.call.contextParams.two.main_two;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_two main,
        helper_two helper
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(main);
        taskDefs.add(helper);
        return taskDefs;
    }
}
