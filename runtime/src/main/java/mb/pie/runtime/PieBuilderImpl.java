package mb.pie.runtime;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.PieBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.FileSystemStampers;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.NoopLogger;
import mb.pie.runtime.logger.exec.LoggerExecutorLogger;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.taskdefs.NullTaskDefs;
import mb.resource.DefaultResourceService;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.fs.FSRegistry;
import mb.resource.fs.FSResource;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PieBuilderImpl implements PieBuilder {
    private TaskDefs taskDefs = new NullTaskDefs();
    private ResourceService resourceService = new DefaultResourceService(new FSRegistry());
    private Function<Logger, Store> store = (logger) -> new InMemoryStore();
    private Function<Logger, Share> share = (logger) -> new NonSharingShare();
    private OutputStamper defaultOutputStamper = OutputStampers.equals();
    private ResourceStamper<ReadableResource> defaultRequireReadableStamper = FileSystemStampers.modified();
    private ResourceStamper<ReadableResource> defaultProvideReadableStamper = FileSystemStampers.modified();
    private ResourceStamper<FSResource> defaultRequireFileSystemStamper = FileSystemStampers.modified();
    private ResourceStamper<FSResource> defaultProvideFileSystemStamper = FileSystemStampers.modified();
    private BiFunction<TaskDefs, Logger, Layer> layerFactory = ValidationLayer::new;
    private Logger logger = new NoopLogger();
    private Function<Logger, ExecutorLogger> executorLoggerFactory = LoggerExecutorLogger::new;

    @Override public PieBuilderImpl withTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
        return this;
    }

    @Override public PieBuilder withResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
        return this;
    }

    @Override public PieBuilderImpl withStore(Function<Logger, Store> store) {
        this.store = store;
        return this;
    }

    @Override public PieBuilderImpl withShare(Function<Logger, Share> share) {
        this.share = share;
        return this;
    }

    @Override public PieBuilderImpl withDefaultOutputStamper(OutputStamper stamper) {
        this.defaultOutputStamper = stamper;
        return this;
    }

    @Override public PieBuilder withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultRequireReadableStamper = stamper;
        return this;
    }

    @Override public PieBuilder withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultProvideReadableStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withDefaultRequireFSResourceStamper(ResourceStamper<FSResource> stamper) {
        this.defaultRequireFileSystemStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withDefaultProvideFSResourceStamper(ResourceStamper<FSResource> stamper) {
        this.defaultProvideFileSystemStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withLayer(BiFunction<TaskDefs, Logger, Layer> layer) {
        this.layerFactory = layer;
        return this;
    }

    @Override public PieBuilderImpl withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override public PieBuilderImpl withExecutorLogger(Function<Logger, ExecutorLogger> executorLogger) {
        this.executorLoggerFactory = executorLogger;
        return this;
    }

    @Override public PieImpl build() {
        final Store store = this.store.apply(logger);
        final Share share = this.share.apply(logger);
        final DefaultStampers defaultStampers =
            new DefaultStampers(defaultOutputStamper, defaultRequireReadableStamper, defaultProvideReadableStamper,
                defaultRequireFileSystemStamper, defaultProvideFileSystemStamper);
        return new PieImpl(taskDefs, resourceService, store, share, defaultStampers, layerFactory, logger,
            executorLoggerFactory);
    }
}
