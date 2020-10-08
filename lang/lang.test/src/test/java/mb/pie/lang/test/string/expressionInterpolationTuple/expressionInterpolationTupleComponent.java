package mb.pie.lang.test.string.expressionInterpolationTuple;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationTupleComponent extends PieComponent {
    main_expressionInterpolationTuple get();
}
