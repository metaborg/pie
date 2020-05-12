package mb.pie.lang.test.binary.neq.neqListEqualEmpty;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqListEqualEmptyComponent extends PieComponent {
    main_neqListEqualEmpty get();
}
