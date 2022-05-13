package mb.pie.lang.test.call.func.twoParamBothAnonymous;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface twoParamBothAnonymousComponent extends PieComponent {
    main_twoParamBothAnonymous get();
}
