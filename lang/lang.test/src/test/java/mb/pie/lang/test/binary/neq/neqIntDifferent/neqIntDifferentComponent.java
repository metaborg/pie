package mb.pie.lang.test.binary.neq.neqIntDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqIntDifferentComponent extends PieComponent {
    main_neqIntDifferent get();
}
