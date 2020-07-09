package mb.pie.lang.test.funcDef.twoFuncLinear;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface twoFuncLinearComponent extends PieComponent {
    main_twoFuncLinear get();
}
