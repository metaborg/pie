package mb.pie.lang.test.string.variableInterpolationEscapeAndInterpolation;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationEscapeAndInterpolationComponent extends PieComponent {
    main_variableInterpolationEscapeAndInterpolation get();
}
