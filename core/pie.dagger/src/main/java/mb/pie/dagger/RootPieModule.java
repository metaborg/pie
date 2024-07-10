package mb.pie.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.log.api.LoggerFactory;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.PieBuilder.LayerFactory;
import mb.pie.api.PieBuilder.StoreFactory;
import mb.pie.api.Share;
import mb.pie.api.TaskDef;
import mb.pie.api.Tracer;
import mb.pie.api.serde.Serde;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Named;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Module(includes = PieProviderModule.class)
public class RootPieModule {
    private final Supplier<PieBuilder> builderSupplier;
    private final Set<TaskDef<?, ?>> taskDefs;
    private @Nullable Function<LoggerFactory, Serde> serdeFactory;
    private @Nullable StoreFactory storeFactory;
    private @Nullable Function<LoggerFactory, Share> shareFactory;
    private @Nullable OutputStamper defaultOutputStamper;
    private @Nullable ResourceStamper<ReadableResource> defaultRequireReadableResourceStamper;
    private @Nullable ResourceStamper<ReadableResource> defaultProvideReadableResourceStamper;
    private @Nullable ResourceStamper<HierarchicalResource> defaultRequireHierarchicalResourceStamper;
    private @Nullable ResourceStamper<HierarchicalResource> defaultProvideHierarchicalResourceStamper;
    private @Nullable LayerFactory layerFactory;
    private @Nullable Function<LoggerFactory, Tracer> tracerFactory;


    public RootPieModule(Supplier<PieBuilder> builderSupplier, Set<TaskDef<?, ?>> taskDefs) {
        this.builderSupplier = builderSupplier;
        this.taskDefs = taskDefs;
    }

    public RootPieModule(Supplier<PieBuilder> builderSupplier, TaskDef<?, ?>... taskDefs) {
        this(builderSupplier, new HashSet<>(Arrays.asList(taskDefs)));
    }

    public RootPieModule(Supplier<PieBuilder> builderSupplier, TaskDefsProvider taskDefsComponent) {
        this(builderSupplier, new HashSet<>(taskDefsComponent.getTaskDefs()));
    }

    public RootPieModule(Supplier<PieBuilder> builderSupplier) {
        this(builderSupplier, new HashSet<>());
    }


    public RootPieModule addTaskDef(TaskDef<?, ?> taskDef) {
        taskDefs.add(taskDef);
        return this;
    }

    public RootPieModule addTaskDefs(TaskDef<?, ?>... taskDefs) {
        this.taskDefs.addAll(Arrays.asList(taskDefs));
        return this;
    }

    public RootPieModule addTaskDefs(Iterable<TaskDef<?, ?>> taskDefs) {
        for(TaskDef<?, ?> taskDef : taskDefs) {
            this.taskDefs.add(taskDef);
        }
        return this;
    }

    public RootPieModule addTaskDefsFrom(TaskDefsProvider taskDefsComponent) {
        taskDefs.addAll(taskDefsComponent.getTaskDefs());
        return this;
    }

    public RootPieModule withSerdeFactory(Function<LoggerFactory, Serde> serdeFactory) {
        this.serdeFactory = serdeFactory;
        return this;
    }

    public RootPieModule withStoreFactory(StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
        return this;
    }

    public RootPieModule withShareFactory(Function<LoggerFactory, Share> shareFactory) {
        this.shareFactory = shareFactory;
        return this;
    }

    public RootPieModule withDefaultOutputStamper(OutputStamper outputStamper) {
        this.defaultOutputStamper = outputStamper;
        return this;
    }

    public RootPieModule withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultRequireReadableResourceStamper = stamper;
        return this;
    }

    public RootPieModule withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultProvideReadableResourceStamper = stamper;
        return this;
    }

    public RootPieModule withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultRequireHierarchicalResourceStamper = stamper;
        return this;
    }

    public RootPieModule withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultProvideHierarchicalResourceStamper = stamper;
        return this;
    }

    public RootPieModule withLayerFactory(LayerFactory layerFactory) {
        this.layerFactory = layerFactory;
        return this;
    }

    public RootPieModule withTracerFactory(Function<LoggerFactory, Tracer> tracerFactory) {
        this.tracerFactory = tracerFactory;
        return this;
    }


    @Provides @Named("parent") @PieScope
    static Optional<Pie> provideParentPie() {
        return Optional.empty();
    }

    @Provides /* Unscoped: new builder every time */
    Optional<Provider<PieBuilder>> providePieBuilder() {
        return Optional.of(builderSupplier::get);
    }

    @Provides @PieScope @ElementsIntoSet
    Set<TaskDef<?, ?>> provideTaskDefs() {
        return taskDefs;
    }

    @Provides @PieScope
    Optional<Function<LoggerFactory, Serde>> provideSerdeFactory() {
        return Optional.ofNullable(serdeFactory);
    }

    @Provides @PieScope
    Optional<StoreFactory> provideStoreFactory() {
        return Optional.ofNullable(storeFactory);
    }

    @Provides @PieScope
    Optional<Function<LoggerFactory, Share>> provideShareFactory() {
        return Optional.ofNullable(shareFactory);
    }

    @Provides @PieScope
    Optional<OutputStamper> provideDefaultOutputStamper() {
        return Optional.ofNullable(defaultOutputStamper);
    }

    @Provides @Named("require") @PieScope
    Optional<ResourceStamper<ReadableResource>> provideDefaultRequireReadableResourceStamper() {
        return Optional.ofNullable(defaultRequireReadableResourceStamper);
    }

    @Provides @Named("provide") @PieScope
    Optional<ResourceStamper<ReadableResource>> provideDefaultProvideReadableResourceStamper() {
        return Optional.ofNullable(defaultProvideReadableResourceStamper);
    }

    @Provides @Named("require") @PieScope
    Optional<ResourceStamper<HierarchicalResource>> provideDefaultRequireHierarchicalResourceStamper() {
        return Optional.ofNullable(defaultRequireHierarchicalResourceStamper);
    }

    @Provides @Named("provide") @PieScope
    Optional<ResourceStamper<HierarchicalResource>> provideDefaultProvideHierarchicalResourceStamper() {
        return Optional.ofNullable(defaultProvideHierarchicalResourceStamper);
    }

    @Provides @PieScope
    Optional<LayerFactory> provideLayerFactory() {
        return Optional.ofNullable(layerFactory);
    }

    @Provides @PieScope
    Optional<Function<LoggerFactory, Tracer>> provideTracerFactory() {
        return Optional.ofNullable(tracerFactory);
    }
}
