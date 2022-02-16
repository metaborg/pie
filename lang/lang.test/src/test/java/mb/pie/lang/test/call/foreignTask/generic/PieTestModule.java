package mb.pie.lang.test.call.foreignTask.generic;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.call.foreignTask.generic.main_generic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        Generic<Integer, ArrayList<Integer>> generic,
        main_generic main_generic
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(generic);
        taskDefs.add(main_generic);
        return taskDefs;
    }

    @Provides @mb.pie.dagger.PieScope
    public static Generic<Integer, ArrayList<Integer>> provideGeneric() {
        return new Generic<>();
    }
}
