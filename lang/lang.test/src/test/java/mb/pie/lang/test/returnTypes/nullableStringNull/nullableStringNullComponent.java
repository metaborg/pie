package mb.pie.lang.test.returnTypes.nullableStringNull;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface nullableStringNullComponent extends PieComponent {
    main_nullableStringNull get();
}
