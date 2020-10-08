package mb.pie.lang.test.string.expressionInterpolationNothingBetween;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationNothingBetweenComponent extends PieComponent {
    main_expressionInterpolationNothingBetween get();
}
