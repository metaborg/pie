package mb.pie.lang.test.imports.subModule.importPartialModulePathForeignPie;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.imports.subModule.a.b.c.ForeignPie;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @Singleton @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_importPartialModulePathForeignPie importPartialModulePathForeignPie,
        ForeignPie foreignPie
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(importPartialModulePathForeignPie);
        taskDefs.add(foreignPie);
        return taskDefs;
    }
}
