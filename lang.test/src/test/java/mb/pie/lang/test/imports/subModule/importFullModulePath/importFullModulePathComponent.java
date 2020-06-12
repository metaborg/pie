package mb.pie.lang.test.imports.subModule.importFullModulePath;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullModulePathComponent extends PieComponent {
    main_importFullModulePath get();
}
