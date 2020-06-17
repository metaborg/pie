package mb.pie.lang.test.unary.toNonNullableValue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface toNonNullableValueComponent extends PieComponent {
    main_toNonNullableValue get();
}
