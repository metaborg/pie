package mb.pie.lang.test.funcDef.twoFuncUnused;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface twoFuncUnusedComponent extends PieComponent {
    main_twoFuncUnused get();
}
