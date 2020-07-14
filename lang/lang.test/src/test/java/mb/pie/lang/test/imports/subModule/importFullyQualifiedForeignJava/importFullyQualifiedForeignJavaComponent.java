package mb.pie.lang.test.imports.subModule.importFullyQualifiedForeignJava;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullyQualifiedForeignJavaComponent extends PieComponent {
    main_importFullyQualifiedForeignJava get();
}
