package mb.pie.lang.test.binary.lor.lorTrueTrue;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface lorTrueTrueComponent extends PieComponent {
    main_lorTrueTrue get();
}
