package mb.pie.lang.test.string.variableInterpolationNothingBetween;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationNothingBetweenComponent extends PieComponent {
    main_variableInterpolationNothingBetween get();
}
