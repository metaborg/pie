package mb.pie.lang.test.imports.subModule.importFullyQualifiedForeignPie;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullyQualifiedForeignPieComponent extends PieComponent {
    main_importFullyQualifiedForeignPie get();
}
