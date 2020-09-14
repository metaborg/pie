package mb.pie.lang.test.string.expressionInterpolationVariableBetween;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationVariableBetweenComponent extends PieComponent {
    main_expressionInterpolationVariableBetween get();
}
