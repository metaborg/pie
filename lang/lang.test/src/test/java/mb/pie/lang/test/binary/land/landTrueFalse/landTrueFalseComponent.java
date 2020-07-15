package mb.pie.lang.test.binary.land.landTrueFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface landTrueFalseComponent extends PieComponent {
    main_landTrueFalse get();
}
