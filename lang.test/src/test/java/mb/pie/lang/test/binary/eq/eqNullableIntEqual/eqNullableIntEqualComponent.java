package mb.pie.lang.test.binary.eq.eqNullableIntEqual;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface eqNullableIntEqualComponent extends PieComponent {
    main_eqNullableIntEqual get();
}
