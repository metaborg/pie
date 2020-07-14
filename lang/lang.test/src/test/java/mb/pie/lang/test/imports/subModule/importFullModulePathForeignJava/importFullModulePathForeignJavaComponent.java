package mb.pie.lang.test.imports.subModule.importFullModulePathForeignJava;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullModulePathForeignJavaComponent extends PieComponent {
    main_importFullModulePathForeignJava get();
}
