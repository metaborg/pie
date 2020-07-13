package mb.pie.lang.test.binary.neq.neqListEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface neqListEqualComponent extends PieComponent {
    main_neqListEqual get();
}
