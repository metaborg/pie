package mb.pie.lang.test.imports.subModule.importPartialModulePathData;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importPartialModulePathDataComponent extends PieComponent {
    main_importPartialModulePathData get();
}
