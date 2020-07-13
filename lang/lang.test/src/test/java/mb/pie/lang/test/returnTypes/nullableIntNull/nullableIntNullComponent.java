package mb.pie.lang.test.returnTypes.nullableIntNull;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface nullableIntNullComponent extends PieComponent {
    main_nullableIntNull get();
}
