package mb.pie.lang.test.binary.neq.neqBoolEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqBoolEqualComponent extends PieComponent {
    main_neqBoolEqual get();
}
