package mb.pie.lang.test.string.variableInterpolationDoubleNothingBetweenIntString;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationDoubleNothingBetweenIntStringComponent extends PieComponent {
    main_variableInterpolationDoubleNothingBetweenIntString get();
}
