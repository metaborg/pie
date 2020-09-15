package mb.pie.lang.test.string.expressionInterpolationList;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface expressionInterpolationListComponent extends PieComponent {
    main_expressionInterpolationList get();
}
