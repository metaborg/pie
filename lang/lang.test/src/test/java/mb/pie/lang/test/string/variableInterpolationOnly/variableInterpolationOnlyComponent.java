package mb.pie.lang.test.string.variableInterpolationOnly;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationOnlyComponent extends PieComponent {
    main_variableInterpolationOnly get();
}
