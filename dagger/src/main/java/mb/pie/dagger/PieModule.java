package mb.pie.dagger;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.taskdefs.MapTaskDefs;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.fs.FSResource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Module public abstract class PieModule {
    @Provides @Singleton static Pie providePie(
        Set<TaskDef<?, ?>> taskDefs,
        Optional<ResourceService> resourceService,
        Optional<Function<Logger, Store>> storeFunc,
        Optional<Function<Logger, Share>> shareFunc,
        Optional<OutputStamper> defaultOutputStamper,
        @Named("require") Optional<ResourceStamper<ReadableResource>> defaultRequireReadableResourceStamper,
        @Named("provide") Optional<ResourceStamper<ReadableResource>> defaultProvideReadableResourceStamper,
        @Named("require") Optional<ResourceStamper<FSResource>> defaultRequireFSResourceStamper,
        @Named("provide") Optional<ResourceStamper<FSResource>> defaultProvideFSResourceStamper,
        Optional<Function<Logger, Layer>> layerFunc,
        Optional<Logger> logger,
        Optional<Function<Logger, ExecutorLogger>> executorLoggerFunc
    ) {
        final PieBuilder builder = new PieBuilderImpl();
        builder.withTaskDefs(new MapTaskDefs(taskDefs));
        resourceService.ifPresent(builder::withResourceService);
        storeFunc.ifPresent(builder::withStore);
        shareFunc.ifPresent(builder::withShare);
        defaultOutputStamper.ifPresent(builder::withDefaultOutputStamper);
        defaultRequireReadableResourceStamper.ifPresent(builder::withDefaultRequireReadableResourceStamper);
        defaultProvideReadableResourceStamper.ifPresent(builder::withDefaultProvideReadableResourceStamper);
        defaultRequireFSResourceStamper.ifPresent(builder::withDefaultRequireFSResourceStamper);
        defaultProvideFSResourceStamper.ifPresent(builder::withDefaultProvideFSResourceStamper);
        layerFunc.ifPresent(builder::withLayer);
        logger.ifPresent(builder::withLogger);
        executorLoggerFunc.ifPresent(builder::withExecutorLogger);
        return builder.build();
    }

    @Provides @Singleton @ElementsIntoSet static Set<TaskDef<?, ?>> provideTaskDefs() {
        return new HashSet<>();
    }


    @BindsOptionalOf abstract ResourceService resourceService();

    @BindsOptionalOf abstract Function<Logger, Store> storeFunc();

    @BindsOptionalOf abstract Function<Logger, Share> shareFunc();

    @BindsOptionalOf abstract OutputStamper defaultOutputStamper();

    @BindsOptionalOf @Named("require")
    abstract ResourceStamper<ReadableResource> defaultRequireReadableResourceStamper();

    @BindsOptionalOf @Named("provide")
    abstract ResourceStamper<ReadableResource> defaultProvideReadableResourceStamper();

    @BindsOptionalOf @Named("require")
    abstract ResourceStamper<FSResource> defaultRequireFSResourceStamper();

    @BindsOptionalOf @Named("provide")
    abstract ResourceStamper<FSResource> defaultProvideFSResourceStamper();

    @BindsOptionalOf abstract Function<Logger, Layer> layerFunc();

    @BindsOptionalOf abstract Logger logger();

    @BindsOptionalOf abstract Function<Logger, ExecutorLogger> execLoggerFunc();
}
