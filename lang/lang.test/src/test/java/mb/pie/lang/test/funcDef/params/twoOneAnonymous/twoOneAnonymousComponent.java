package mb.pie.lang.test.funcDef.params.twoOneAnonymous;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface twoOneAnonymousComponent extends PieComponent {
    main_twoOneAnonymous get();
}
