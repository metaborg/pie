package mb.pie.lang.test.binary.eq.eqNullableIntDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqNullableIntDifferentComponent extends PieComponent {
    main_eqNullableIntDifferent get();
}
