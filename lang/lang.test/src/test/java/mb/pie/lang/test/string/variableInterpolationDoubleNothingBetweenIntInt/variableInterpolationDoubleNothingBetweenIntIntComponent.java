package mb.pie.lang.test.string.variableInterpolationDoubleNothingBetweenIntInt;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationDoubleNothingBetweenIntIntComponent extends PieComponent {
    main_variableInterpolationDoubleNothingBetweenIntInt get();
}
