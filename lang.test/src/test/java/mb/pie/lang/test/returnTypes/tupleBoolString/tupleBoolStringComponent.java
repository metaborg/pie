package mb.pie.lang.test.returnTypes.tupleBoolString;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface tupleBoolStringComponent extends PieComponent {
    main_tupleBoolString get();
}
