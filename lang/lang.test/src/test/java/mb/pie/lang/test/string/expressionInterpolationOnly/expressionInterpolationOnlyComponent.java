package mb.pie.lang.test.string.expressionInterpolationOnly;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationOnlyComponent extends PieComponent {
    main_expressionInterpolationOnly get();
}
