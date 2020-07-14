package mb.pie.lang.test.binary.neq.neqDataEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqDataEqualComponent extends PieComponent {
    main_neqDataEqual get();
}
