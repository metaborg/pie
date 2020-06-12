package mb.pie.lang.test.binary.eq.eqPathEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqPathEqualComponent extends PieComponent {
    main_eqPathEqual get();
}
