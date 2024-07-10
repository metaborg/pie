package mb.pie.lang.test.supplier.fromTaskAndGet.fullyQualifiedSupplier;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.supplier.fromTaskAndGet.fullyQualifiedSupplier.main_fullyQualifiedSupplier;
import mb.pie.lang.test.supplier.otherPackage.helper.helper_helloName;

import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_fullyQualifiedSupplier fullyQualifiedSupplier,
        helper_helloName helper
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(1, 1);
        taskDefs.add(fullyQualifiedSupplier);
        taskDefs.add(helper);
        return taskDefs;
    }
}
