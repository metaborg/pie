package mb.pie.lang.test.supplier.createAndGet.typeInt;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface typeIntComponent extends PieComponent {
    main_typeInt get();
}
