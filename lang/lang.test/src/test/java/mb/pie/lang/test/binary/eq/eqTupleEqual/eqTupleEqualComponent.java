package mb.pie.lang.test.binary.eq.eqTupleEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqTupleEqualComponent extends PieComponent {
    main_eqTupleEqual get();
}
