package mb.pie.lang.test.binary.eq.eqNullableIntNull;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqNullableIntNullComponent extends PieComponent {
    main_eqNullableIntNull get();
}
