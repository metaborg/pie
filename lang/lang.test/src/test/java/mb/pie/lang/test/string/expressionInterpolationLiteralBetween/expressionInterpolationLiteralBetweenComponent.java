package mb.pie.lang.test.string.expressionInterpolationLiteralBetween;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationLiteralBetweenComponent extends PieComponent {
    main_expressionInterpolationLiteralBetween get();
}
