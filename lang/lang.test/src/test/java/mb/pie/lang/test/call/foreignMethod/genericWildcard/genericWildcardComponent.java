package mb.pie.lang.test.call.foreignMethod.genericWildcard;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;
import mb.pie.lang.test.call.foreignMethod.genericWildcard.main_genericWildcard;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface genericWildcardComponent extends PieComponent {
    main_genericWildcard get();
}
