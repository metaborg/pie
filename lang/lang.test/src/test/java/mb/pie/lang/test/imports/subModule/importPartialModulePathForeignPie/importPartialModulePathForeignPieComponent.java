package mb.pie.lang.test.imports.subModule.importPartialModulePathForeignPie;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importPartialModulePathForeignPieComponent extends PieComponent {
    main_importPartialModulePathForeignPie get();
}
