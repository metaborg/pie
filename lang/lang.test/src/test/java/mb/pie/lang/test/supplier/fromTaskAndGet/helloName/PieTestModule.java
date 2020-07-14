package mb.pie.lang.test.supplier.fromTaskAndGet.helloName;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.supplier.fromTaskAndGet.helloName.helper_helloName;
import mb.pie.lang.test.supplier.fromTaskAndGet.helloName.main_helloName;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @Singleton @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_helloName helloName,
        helper_helloName helper
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(helloName);
        taskDefs.add(helper);
        return taskDefs;
    }
}
