package mb.pie.dagger;

import dagger.Component;
import mb.pie.api.ResourceSystem;

import java.util.Set;

@Component(modules = ResourceSystemsModule.class)
public interface ResourceSystemsComponent {
    Set<ResourceSystem> getResourceSystems();
}
