package mb.pie.lang.test.call.contextParams.one;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.contextParams.one.main_one;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface oneComponent extends PieComponent {
    main_one get();
}
