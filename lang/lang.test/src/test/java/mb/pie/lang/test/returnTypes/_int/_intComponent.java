package mb.pie.lang.test.returnTypes._int;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface _intComponent extends PieComponent {
    main__int get();
}
