package mb.pie.runtime;

import mb.log.api.LoggerFactory;
import mb.log.noop.NoopLoggerFactory;
import mb.pie.api.Layer;
import mb.pie.api.PieBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.Tracer;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.tracer.NoopTracer;
import mb.resource.DefaultResourceService;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.classloader.ClassLoaderResourceRegistry;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.text.TextResourceRegistry;
import mb.resource.url.URLResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PieBuilderImpl implements PieBuilder {
    protected @Nullable TaskDefs taskDefs;
    protected ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry(), new URLResourceRegistry(), new TextResourceRegistry(), new ClassLoaderResourceRegistry(PieBuilderImpl.class.getClassLoader()));
    protected BiFunction<LoggerFactory, ResourceService, Store> storeFactory = (logger, resourceService) -> new InMemoryStore();
    protected Function<LoggerFactory, Share> shareFactory = (logger) -> new NonSharingShare();
    protected OutputStamper defaultOutputStamper = OutputStampers.equals();
    protected ResourceStamper<ReadableResource> defaultRequireReadableStamper = ResourceStampers.modifiedFile();
    protected ResourceStamper<ReadableResource> defaultProvideReadableStamper = ResourceStampers.modifiedFile();
    protected ResourceStamper<HierarchicalResource> defaultRequireHierarchicalStamper = ResourceStampers.modifiedFile();
    protected ResourceStamper<HierarchicalResource> defaultProvideHierarchicalStamper = ResourceStampers.modifiedFile();
    protected BiFunction<TaskDefs, LoggerFactory, Layer> layerFactory = ValidationLayer::new;
    protected LoggerFactory loggerFactory = NoopLoggerFactory.instance;
    protected Function<LoggerFactory, Tracer> tracerFactory = (logger) -> NoopTracer.instance;


    @Override
    public PieBuilderImpl withTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
        return this;
    }

    @Override
    public PieBuilderImpl withResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
        return this;
    }

    @Override
    public PieBuilderImpl withStoreFactory(BiFunction<LoggerFactory, ResourceService, Store> storeFactory) {
        this.storeFactory = storeFactory;
        return this;
    }

    @Override
    public PieBuilderImpl withShareFactory(Function<LoggerFactory, Share> shareFactory) {
        this.shareFactory = shareFactory;
        return this;
    }

    @Override
    public PieBuilderImpl withDefaultOutputStamper(OutputStamper stamper) {
        this.defaultOutputStamper = stamper;
        return this;
    }

    @Override
    public PieBuilderImpl withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultRequireReadableStamper = stamper;
        return this;
    }

    @Override
    public PieBuilderImpl withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultProvideReadableStamper = stamper;
        return this;
    }

    @Override
    public PieBuilderImpl withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultRequireHierarchicalStamper = stamper;
        return this;
    }

    @Override
    public PieBuilderImpl withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultProvideHierarchicalStamper = stamper;
        return this;
    }

    @Override
    public PieBuilderImpl withLayerFactory(BiFunction<TaskDefs, LoggerFactory, Layer> layerFactory) {
        this.layerFactory = layerFactory;
        return this;
    }

    @Override
    public PieBuilderImpl withLoggerFactory(LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
        return this;
    }

    @Override
    public PieBuilderImpl withTracerFactory(Function<LoggerFactory, Tracer> tracerFactory) {
        this.tracerFactory = tracerFactory;
        return this;
    }


    @Override public PieImpl build() {
        if(taskDefs == null) {
            throw new RuntimeException("Task definitions have not been set. Call PieBuilder#withTaskDefs to set task definitions");
        }
        final DefaultStampers defaultStampers = new DefaultStampers(
            defaultOutputStamper,
            defaultRequireReadableStamper,
            defaultProvideReadableStamper,
            defaultRequireHierarchicalStamper,
            defaultProvideHierarchicalStamper
        );
        return new PieImpl(
            taskDefs,
            resourceService,
            storeFactory.apply(loggerFactory, resourceService),
            shareFactory.apply(loggerFactory),
            defaultStampers,
            layerFactory,
            loggerFactory,
            tracerFactory,
            new MapCallbacks()
        );
    }
}
