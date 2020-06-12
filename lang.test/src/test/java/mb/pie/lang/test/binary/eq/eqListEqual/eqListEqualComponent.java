package mb.pie.lang.test.binary.eq.eqListEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqListEqualComponent extends PieComponent {
    main_eqListEqual get();
}
