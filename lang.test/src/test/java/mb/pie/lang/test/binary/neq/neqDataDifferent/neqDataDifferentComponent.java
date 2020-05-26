package mb.pie.lang.test.binary.neq.neqDataDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqDataDifferentComponent extends PieComponent {
    main_neqDataDifferent get();
}
