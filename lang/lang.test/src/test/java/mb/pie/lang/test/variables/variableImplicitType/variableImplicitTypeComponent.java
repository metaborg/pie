package mb.pie.lang.test.variables.variableImplicitType;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableImplicitTypeComponent extends PieComponent {
    main_variableImplicitType get();
}
