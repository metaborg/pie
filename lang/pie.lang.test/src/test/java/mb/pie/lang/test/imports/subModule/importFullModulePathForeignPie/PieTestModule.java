package mb.pie.lang.test.imports.subModule.importFullModulePathForeignPie;

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
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_importFullModulePathForeignPie importFullModulePathForeignPie,
        ForeignPie foreignPie
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(importFullModulePathForeignPie);
        taskDefs.add(foreignPie);
        return taskDefs;
    }
}
