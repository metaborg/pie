package mb.pie.lang.test.unary.toNullable;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface toNullableComponent extends PieComponent {
    main_toNullable get();
}
