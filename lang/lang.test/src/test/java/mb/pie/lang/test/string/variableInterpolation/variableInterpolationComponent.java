package mb.pie.lang.test.string.variableInterpolation;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationComponent extends PieComponent {
    main_variableInterpolation get();
}
