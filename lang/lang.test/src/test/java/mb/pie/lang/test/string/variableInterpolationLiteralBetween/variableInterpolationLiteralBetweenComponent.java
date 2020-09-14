package mb.pie.lang.test.string.variableInterpolationLiteralBetween;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationLiteralBetweenComponent extends PieComponent {
    main_variableInterpolationLiteralBetween get();
}
