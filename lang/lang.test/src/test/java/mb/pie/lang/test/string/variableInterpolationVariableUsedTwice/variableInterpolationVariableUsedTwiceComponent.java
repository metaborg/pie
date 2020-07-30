package mb.pie.lang.test.string.variableInterpolationVariableUsedTwice;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableInterpolationVariableUsedTwiceComponent extends PieComponent {
    main_variableInterpolationVariableUsedTwice get();
}
