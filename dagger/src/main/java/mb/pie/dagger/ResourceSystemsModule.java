package mb.pie.dagger;

import dagger.Module;
import dagger.multibindings.Multibinds;
import mb.pie.api.ResourceSystem;

import java.util.Set;

@Module
public abstract class ResourceSystemsModule {
    @Multibinds abstract Set<ResourceSystem> multibindsResourceSystems();
}
