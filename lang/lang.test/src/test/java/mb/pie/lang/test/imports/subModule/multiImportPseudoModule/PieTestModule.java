package mb.pie.lang.test.imports.subModule.multiImportPseudoModule;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.TaskDef;
import mb.pie.lang.test.imports.subModule.a.b.c.helper_takeFoo;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Module
abstract class PieTestModule {
    @Provides @mb.pie.dagger.PieScope @ElementsIntoSet
    public static Set<TaskDef<?, ?>> provideTaskDefs(
        main_multiImportPseudoModule multiImportPseudoModule,
        helper_takeFoo helper_takeFoo
    ) {
        final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>(2, 1);
        taskDefs.add(multiImportPseudoModule);
        taskDefs.add(helper_takeFoo);
        return taskDefs;
    }
}
