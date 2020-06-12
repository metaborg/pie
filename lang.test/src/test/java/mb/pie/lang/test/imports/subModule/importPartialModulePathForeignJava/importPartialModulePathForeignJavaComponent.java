package mb.pie.lang.test.imports.subModule.importPartialModulePathForeignJava;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importPartialModulePathForeignJavaComponent extends PieComponent {
    main_importPartialModulePathForeignJava get();
}
