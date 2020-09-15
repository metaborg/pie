package mb.pie.lang.test.string.expressionInterpolationVariable;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationVariableComponent extends PieComponent {
    main_expressionInterpolationVariable get();
}
