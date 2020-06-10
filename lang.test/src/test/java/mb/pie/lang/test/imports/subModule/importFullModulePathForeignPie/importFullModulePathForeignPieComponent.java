package mb.pie.lang.test.imports.subModule.importFullModulePathForeignPie;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullModulePathForeignPieComponent extends PieComponent {
    main_importFullModulePathForeignPie get();
}
