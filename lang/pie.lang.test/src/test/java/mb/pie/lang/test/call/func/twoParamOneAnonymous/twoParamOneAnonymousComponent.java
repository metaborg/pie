package mb.pie.lang.test.call.func.twoParamOneAnonymous;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface twoParamOneAnonymousComponent extends PieComponent {
    main_twoParamOneAnonymous get();
}
