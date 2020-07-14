package mb.pie.lang.test.returnTypes.nullableIntValue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface nullableIntValueComponent extends PieComponent {
    main_nullableIntValue get();
}
