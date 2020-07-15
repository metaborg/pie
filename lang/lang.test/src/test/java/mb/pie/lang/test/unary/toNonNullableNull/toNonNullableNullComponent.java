package mb.pie.lang.test.unary.toNonNullableNull;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface toNonNullableNullComponent extends PieComponent {
    main_toNonNullableNull get();
}
