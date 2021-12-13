package mb.pie.bench.state;

import mb.log.api.Logger;
import mb.log.dagger.LoggerComponent;
import mb.pie.bench.spoofax3.Spoofax3CompilerState;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Thread)
public class ResourcesState {
    // Trial

    private @Nullable Logger logger;
    private @Nullable ClassLoaderResourceRegistry classLoaderResourceRegistry;
    private @Nullable RootResourceServiceComponent resourceServiceComponent;

    public ResourcesState setupTrial(LoggerComponent loggerComponent) {
        if(resourceServiceComponent != null && classLoaderResourceRegistry != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        logger = loggerComponent.getLoggerFactory().create(Spoofax3CompilerState.class);
        logger.trace("{}.setupTrial", getClass().getName());
        classLoaderResourceRegistry = new ClassLoaderResourceRegistry("pie.bench", ResourcesState.class.getClassLoader());
        resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .loggerComponent(loggerComponent)
            .rootResourceServiceModule(new RootResourceServiceModule(classLoaderResourceRegistry))
            .build();
        return this;
    }

    @SuppressWarnings("ConstantConditions")
    public ClassLoaderResourceRegistry getClassLoaderResourceRegistry() {
        return classLoaderResourceRegistry;
    }

    @SuppressWarnings("ConstantConditions")
    public RootResourceServiceComponent getResourceServiceComponent() {
        return resourceServiceComponent;
    }

    @SuppressWarnings("ConstantConditions")
    public ResourceService getResourceService() {
        return resourceServiceComponent.getResourceService();
    }

    public void tearDownTrial() {
        if(logger == null || classLoaderResourceRegistry == null || resourceServiceComponent == null) {
            throw new IllegalStateException("tearDownTrial was called before calling setupTrial");
        }
        resourceServiceComponent.close();
        resourceServiceComponent = null;
        classLoaderResourceRegistry = null;
        logger.trace("{}.tearDownTrial", getClass().getName());
        logger = null;
    }
}
