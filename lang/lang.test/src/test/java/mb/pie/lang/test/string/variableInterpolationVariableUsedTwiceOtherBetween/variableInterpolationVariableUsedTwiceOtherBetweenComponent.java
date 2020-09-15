package mb.pie.lang.test.string.variableInterpolationVariableUsedTwiceOtherBetween;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationVariableUsedTwiceOtherBetweenComponent extends PieComponent {
    main_variableInterpolationVariableUsedTwiceOtherBetween get();
}
