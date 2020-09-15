package mb.pie.lang.test.string.expressionInterpolation;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationComponent extends PieComponent {
    main_expressionInterpolation get();
}
