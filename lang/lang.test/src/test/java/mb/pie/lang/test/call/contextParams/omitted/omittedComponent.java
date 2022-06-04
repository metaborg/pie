package mb.pie.lang.test.call.contextParams.omitted;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.contextParams.omitted.main_omitted;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface omittedComponent extends PieComponent {
    main_omitted get();
}
