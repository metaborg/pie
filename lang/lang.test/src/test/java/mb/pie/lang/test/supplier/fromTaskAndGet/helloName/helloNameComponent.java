package mb.pie.lang.test.supplier.fromTaskAndGet.helloName;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.supplier.fromTaskAndGet.helloName.main_helloName;

import javax.inject.Singleton;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface helloNameComponent extends PieComponent {
    main_helloName get();
}
