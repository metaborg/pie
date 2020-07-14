package mb.pie.lang.test.binary.lor.lorFalseTrue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface lorFalseTrueComponent extends PieComponent {
    main_lorFalseTrue get();
}
