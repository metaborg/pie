package mb.pie.lang.test.call.contextParams.empty;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.contextParams.empty.main_empty;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface emptyComponent extends PieComponent {
    main_empty get();
}
