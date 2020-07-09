package mb.pie.lang.test.binary.eq.eqTupleDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqTupleDifferentComponent extends PieComponent {
    main_eqTupleDifferent get();
}
