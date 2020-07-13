package mb.pie.lang.test.supplier.fromTaskAndGet.tripleInt;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface tripleIntComponent extends PieComponent {
    main_tripleInt get();
}
