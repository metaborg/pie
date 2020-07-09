package mb.pie.lang.test.returnTypes.unitNoValue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface unitNoValueComponent extends PieComponent {
    main_unitNoValue get();
}
