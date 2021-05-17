package mb.pie.lang.test.supplier.fromTaskAndGet.fullyQualifiedSupplier;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.supplier.fromTaskAndGet.fullyQualifiedSupplier.main_fullyQualifiedSupplier;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface fullyQualifiedSupplierComponent extends PieComponent {
    main_fullyQualifiedSupplier get();
}
