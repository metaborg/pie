package mb.pie.dagger;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.PieBuilder.LayerFactory;
import mb.pie.api.PieBuilder.StoreFactory;
import mb.pie.api.PieChildBuilder;
import mb.pie.api.Share;
import mb.pie.api.TaskDef;
import mb.pie.api.Tracer;
import mb.pie.api.serde.Serde;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import javax.inject.Named;
import javax.inject.Provider;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Module(includes = PieProviderModule.class)
public abstract class PieOptionalsProviderModule {
    @BindsOptionalOf @Named("parent")
    abstract Pie parentPie();

    @BindsOptionalOf
    abstract Supplier<PieBuilder> builderSupplier();

    @BindsOptionalOf
    abstract Function<LoggerFactory, Serde> serdeFactory();

    @BindsOptionalOf
    abstract StoreFactory storeFactory();

    @BindsOptionalOf
    abstract Function<LoggerFactory, Share> shareFactory();

    @BindsOptionalOf
    abstract OutputStamper defaultOutputStamper();

    @BindsOptionalOf @Named("require")
    abstract ResourceStamper<ReadableResource> defaultRequireReadableResourceStamper();

    @BindsOptionalOf @Named("provide")
    abstract ResourceStamper<ReadableResource> defaultProvideReadableResourceStamper();

    @BindsOptionalOf @Named("require")
    abstract ResourceStamper<HierarchicalResource> defaultRequireHierarchicalResourceStamper();

    @BindsOptionalOf @Named("provide")
    abstract ResourceStamper<HierarchicalResource> defaultProvideHierarchicalResourceStamper();

    @BindsOptionalOf
    abstract LayerFactory layerFactory();

    @BindsOptionalOf
    abstract Function<LoggerFactory, Tracer> tracerFactory();

    @Provides @PieScope @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static Pie providePie(
        @Named("parent") Optional<Pie> parentPie,
        Optional<Supplier<PieBuilder>> builderSupplier,
        Set<TaskDef<?, ?>> taskDefs,
        ResourceService resourceService,
        Optional<Function<LoggerFactory, Serde>> serdeFactory,
        Optional<StoreFactory> storeFactory,
        Optional<Function<LoggerFactory, Share>> shareFactory,
        Optional<OutputStamper> defaultOutputStamper,
        @Named("require") Optional<ResourceStamper<ReadableResource>> defaultRequireReadableResourceStamper,
        @Named("provide") Optional<ResourceStamper<ReadableResource>> defaultProvideReadableResourceStamper,
        @Named("require") Optional<ResourceStamper<HierarchicalResource>> defaultRequireHierarchicalResourceStamper,
        @Named("provide") Optional<ResourceStamper<HierarchicalResource>> defaultProvideHierarchicalResourceStamper,
        Optional<LayerFactory> layerFactory,
        Optional<LoggerFactory> loggerFactory,
        Optional<Function<LoggerFactory, Tracer>> tracerFactory
    ) {
        return parentPie.map(p -> {
            final PieChildBuilder builder = p.createChildBuilder();
            builder.withTaskDefs(new MapTaskDefs(taskDefs));
            builder.withResourceService(resourceService);
            defaultOutputStamper.ifPresent(builder::withDefaultOutputStamper);
            defaultRequireReadableResourceStamper.ifPresent(builder::withDefaultRequireReadableResourceStamper);
            defaultProvideReadableResourceStamper.ifPresent(builder::withDefaultProvideReadableResourceStamper);
            defaultRequireHierarchicalResourceStamper.ifPresent(builder::withDefaultRequireHierarchicalResourceStamper);
            defaultProvideHierarchicalResourceStamper.ifPresent(builder::withDefaultProvideHierarchicalResourceStamper);
            layerFactory.ifPresent(builder::withLayerFactory);
            loggerFactory.ifPresent(builder::withLoggerFactory);
            tracerFactory.ifPresent(builder::withTracerFactory);
            return builder.build();
        }).orElseGet(() -> builderSupplier.map(bp -> {
            final PieBuilder builder = bp.get();
            builder.withTaskDefs(new MapTaskDefs(taskDefs));
            builder.withResourceService(resourceService);
            serdeFactory.ifPresent(builder::withSerdeFactory);
            storeFactory.ifPresent(builder::withStoreFactory);
            shareFactory.ifPresent(builder::withShareFactory);
            defaultOutputStamper.ifPresent(builder::withDefaultOutputStamper);
            defaultRequireReadableResourceStamper.ifPresent(builder::withDefaultRequireReadableResourceStamper);
            defaultProvideReadableResourceStamper.ifPresent(builder::withDefaultProvideReadableResourceStamper);
            defaultRequireHierarchicalResourceStamper.ifPresent(builder::withDefaultRequireHierarchicalResourceStamper);
            defaultProvideHierarchicalResourceStamper.ifPresent(builder::withDefaultProvideHierarchicalResourceStamper);
            layerFactory.ifPresent(builder::withLayerFactory);
            loggerFactory.ifPresent(builder::withLoggerFactory);
            tracerFactory.ifPresent(builder::withTracerFactory);
            return builder.build();
        }).orElseThrow(() -> new IllegalStateException("Cannot create PIE instance; PIE builder provider has not been set, nor has a parent PIE instance been set")));
    }
}
