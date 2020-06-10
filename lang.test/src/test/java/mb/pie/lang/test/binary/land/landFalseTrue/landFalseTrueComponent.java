package mb.pie.lang.test.binary.land.landFalseTrue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface landFalseTrueComponent extends PieComponent {
    main_landFalseTrue get();
}
