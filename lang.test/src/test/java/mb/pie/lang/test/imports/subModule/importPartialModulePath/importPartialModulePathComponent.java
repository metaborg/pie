package mb.pie.lang.test.imports.subModule.importPartialModulePath;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importPartialModulePathComponent extends PieComponent {
    main_importPartialModulePath get();
}
