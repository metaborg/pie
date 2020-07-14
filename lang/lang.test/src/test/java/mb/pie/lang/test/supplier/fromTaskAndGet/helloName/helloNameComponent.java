package mb.pie.lang.test.supplier.fromTaskAndGet.helloName;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.supplier.fromTaskAndGet.helloName.main_helloName;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface helloNameComponent extends PieComponent {
    main_helloName get();
}
