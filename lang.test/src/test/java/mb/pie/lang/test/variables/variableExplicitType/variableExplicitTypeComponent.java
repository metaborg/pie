package mb.pie.lang.test.variables.variableExplicitType;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableExplicitTypeComponent extends PieComponent {
    main_variableExplicitType get();
}
