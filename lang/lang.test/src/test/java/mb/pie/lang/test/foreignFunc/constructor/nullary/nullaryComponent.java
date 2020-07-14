package mb.pie.lang.test.foreignFunc.constructor.nullary;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface nullaryComponent extends PieComponent {
    main_nullary get();
}
