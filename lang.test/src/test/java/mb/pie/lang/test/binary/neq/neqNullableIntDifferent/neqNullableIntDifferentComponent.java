package mb.pie.lang.test.binary.neq.neqNullableIntDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqNullableIntDifferentComponent extends PieComponent {
    main_neqNullableIntDifferent get();
}
