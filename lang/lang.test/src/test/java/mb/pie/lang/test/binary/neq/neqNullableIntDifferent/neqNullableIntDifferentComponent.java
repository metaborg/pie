package mb.pie.lang.test.binary.neq.neqNullableIntDifferent;

import dagger.Component;
import mb.pie.dagger.PieComponent;
import mb.pie.dagger.PieModule;

import javax.inject.Singleton;

@mb.pie.dagger.PieScope
@Component(modules = {PieModule.class, PieTestModule.class}, dependencies = {mb.log.dagger.LoggerComponent.class, mb.resource.dagger.ResourceServiceComponent.class})
public interface neqNullableIntDifferentComponent extends PieComponent {
    main_neqNullableIntDifferent get();
}
