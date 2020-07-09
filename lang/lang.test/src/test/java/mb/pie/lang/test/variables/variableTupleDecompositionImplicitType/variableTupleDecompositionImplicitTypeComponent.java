package mb.pie.lang.test.variables.variableTupleDecompositionImplicitType;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableTupleDecompositionImplicitTypeComponent extends PieComponent {
    main_variableTupleDecompositionImplicitType get();
}
