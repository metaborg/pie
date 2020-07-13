package mb.pie.dagger;

import dagger.Module;
import dagger.Provides;
import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Module(includes = PieOptionalsModule.class)
public class PieModule {
    private final Supplier<PieBuilder> builderSupplier;

    public PieModule(Supplier<PieBuilder> builderSupplier) {
        this.builderSupplier = builderSupplier;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @Provides @Singleton
    Pie providePie(
        Set<TaskDef<?, ?>> taskDefs,
        Optional<ResourceService> resourceService,
        Optional<BiFunction<Logger, ResourceService, Store>> storeFunc,
        Optional<Function<Logger, Share>> shareFunc,
        Optional<OutputStamper> defaultOutputStamper,
        @Named("require") Optional<ResourceStamper<ReadableResource>> defaultRequireReadableResourceStamper,
        @Named("provide") Optional<ResourceStamper<ReadableResource>> defaultProvideReadableResourceStamper,
        @Named("require") Optional<ResourceStamper<HierarchicalResource>> defaultRequireHierarchicalResourceStamper,
        @Named("provide") Optional<ResourceStamper<HierarchicalResource>> defaultProvideHierarchicalResourceStamper,
        Optional<BiFunction<TaskDefs, Logger, Layer>> layerFunc,
        Optional<Logger> logger,
        Optional<Function<Logger, ExecutorLogger>> executorLoggerFunc
    ) {
        final PieBuilder builder = builderSupplier.get();
        builder.withTaskDefs(new MapTaskDefs(taskDefs));
        resourceService.ifPresent(builder::withResourceService);
        storeFunc.ifPresent(builder::withStoreFactory);
        shareFunc.ifPresent(builder::withShareFactory);
        defaultOutputStamper.ifPresent(builder::withDefaultOutputStamper);
        defaultRequireReadableResourceStamper.ifPresent(builder::withDefaultRequireReadableResourceStamper);
        defaultProvideReadableResourceStamper.ifPresent(builder::withDefaultProvideReadableResourceStamper);
        defaultRequireHierarchicalResourceStamper.ifPresent(builder::withDefaultRequireHierarchicalResourceStamper);
        defaultProvideHierarchicalResourceStamper.ifPresent(builder::withDefaultProvideHierarchicalResourceStamper);
        layerFunc.ifPresent(builder::withLayerFactory);
        logger.ifPresent(builder::withLogger);
        executorLoggerFunc.ifPresent(builder::withExecutorLoggerFactory);
        return builder.build();
    }
}
