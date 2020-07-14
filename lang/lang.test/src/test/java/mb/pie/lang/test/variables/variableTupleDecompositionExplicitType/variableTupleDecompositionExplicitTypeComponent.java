package mb.pie.lang.test.variables.variableTupleDecompositionExplicitType;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableTupleDecompositionExplicitTypeComponent extends PieComponent {
    main_variableTupleDecompositionExplicitType get();
}
