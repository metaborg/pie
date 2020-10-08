package mb.pie.lang.test.string.variableOnly;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface variableOnlyComponent extends PieComponent {
    main_variableOnly get();
}
