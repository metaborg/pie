package mb.pie.lang.test.binary.eq.eqPathDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqPathDifferentComponent extends PieComponent {
    main_eqPathDifferent get();
}
