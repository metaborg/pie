package mb.pie.lang.test.returnTypes.unitExplicitValue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface unitExplicitValueComponent extends PieComponent {
    main_unitExplicitValue get();
}
