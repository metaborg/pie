package mb.pie.lang.test.binary.eq.eqStringDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqStringDifferentComponent extends PieComponent {
    main_eqStringDifferent get();
}
