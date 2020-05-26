package mb.pie.lang.test.imports.subModule.importFullyQualified;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface importFullyQualifiedComponent extends PieComponent {
    main_importFullyQualified get();
}
