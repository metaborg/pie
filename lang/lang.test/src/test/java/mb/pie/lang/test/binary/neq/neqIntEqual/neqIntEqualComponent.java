package mb.pie.lang.test.binary.neq.neqIntEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqIntEqualComponent extends PieComponent {
    main_neqIntEqual get();
}
