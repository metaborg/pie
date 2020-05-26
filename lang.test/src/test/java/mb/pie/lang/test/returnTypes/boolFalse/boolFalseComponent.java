package mb.pie.lang.test.returnTypes.boolFalse;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface boolFalseComponent extends PieComponent {
    main_boolFalse get();
}
