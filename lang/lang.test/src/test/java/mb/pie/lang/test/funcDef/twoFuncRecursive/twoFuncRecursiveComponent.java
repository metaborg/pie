package mb.pie.lang.test.funcDef.twoFuncRecursive;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface twoFuncRecursiveComponent extends PieComponent {
    main_twoFuncRecursive get();
}
