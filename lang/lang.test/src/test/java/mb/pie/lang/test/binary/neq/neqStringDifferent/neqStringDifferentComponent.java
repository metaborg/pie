package mb.pie.lang.test.binary.neq.neqStringDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqStringDifferentComponent extends PieComponent {
    main_neqStringDifferent get();
}
