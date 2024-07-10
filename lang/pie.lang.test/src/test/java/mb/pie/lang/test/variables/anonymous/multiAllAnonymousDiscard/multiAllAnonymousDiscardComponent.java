package mb.pie.lang.test.variables.anonymous.multiAllAnonymousDiscard;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface multiAllAnonymousDiscardComponent extends PieComponent {
    main_multiAllAnonymousDiscard get();
}
