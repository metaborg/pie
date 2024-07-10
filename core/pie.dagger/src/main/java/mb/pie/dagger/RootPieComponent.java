package mb.pie.dagger;

import dagger.Component;
import mb.log.dagger.LoggerComponent;
import mb.resource.dagger.ResourceServiceComponent;

@PieScope
@Component(
    modules = {
        RootPieModule.class
    },
    dependencies = {
        LoggerComponent.class,
        ResourceServiceComponent.class
    }
)
public interface RootPieComponent extends PieComponent {

}
