package mb.pie.lang.test.supplier.fromTaskAndGet.foreignTask;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PieModule.class, PieTestModule.class})
public interface foreignTaskComponent extends PieComponent {
    main_foreignTask get();
}
