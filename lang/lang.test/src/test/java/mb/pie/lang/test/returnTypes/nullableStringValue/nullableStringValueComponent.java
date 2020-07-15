package mb.pie.lang.test.returnTypes.nullableStringValue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface nullableStringValueComponent extends PieComponent {
    main_nullableStringValue get();
}
