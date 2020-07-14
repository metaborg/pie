package mb.pie.lang.test.binary.land.landFalseFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface landFalseFalseComponent extends PieComponent {
    main_landFalseFalse get();
}
