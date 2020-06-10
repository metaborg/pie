package mb.pie.lang.test.binary.eq.eqListEqualEmpty;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqListEqualEmptyComponent extends PieComponent {
    main_eqListEqualEmpty get();
}
