package mb.pie.lang.test.imports.subModule.importFullModulePathData;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullModulePathDataComponent extends PieComponent {
    main_importFullModulePathData get();
}
