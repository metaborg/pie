package mb.pie.runtime;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.PieBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.fs.FileSystemStampers;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.runtime.exec.BottomUpExecutorImpl;
import mb.pie.runtime.exec.TopDownExecutorImpl;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.NoopLogger;
import mb.pie.runtime.logger.exec.LoggerExecutorLogger;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSRegistry;
import mb.resource.fs.FSResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

public class PieBuilderImpl implements PieBuilder {
    private @Nullable TaskDefs taskDefs = null;
    private @Nullable ResourceRegistry resourceRegistry = null;
    private Function<Logger, Store> store = (logger) -> new InMemoryStore();
    private Function<Logger, Share> share = (logger) -> new NonSharingShare();
    private OutputStamper defaultOutputStamper = OutputStampers.equals();
    private ResourceStamper<FSResource> defaultRequireFileSystemStamper = FileSystemStampers.modified();
    private ResourceStamper<FSResource> defaultProvideFileSystemStamper = FileSystemStampers.modified();
    private Function<Logger, Layer> layerFactory = ValidationLayer::new;
    private Logger logger = new NoopLogger();
    private Function<Logger, ExecutorLogger> executorLoggerFactory = LoggerExecutorLogger::new;


    @Override public PieBuilderImpl withTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
        return this;
    }

    @Override public PieBuilder withResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
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

    @Override public PieBuilderImpl withDefaultRequireFileSystemStamper(ResourceStamper<FSResource> stamper) {
        this.defaultRequireFileSystemStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withDefaultProvideFileSystemStamper(ResourceStamper<FSResource> stamper) {
        this.defaultProvideFileSystemStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withLayer(Function<Logger, Layer> layer) {
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
        final TaskDefs taskDefs;
        if(this.taskDefs != null) {
            taskDefs = this.taskDefs;
        } else {
            throw new RuntimeException("Task definitions were not set before building");
        }

        final ResourceRegistry resourceRegistry;
        if(this.resourceRegistry != null) {
            resourceRegistry = this.resourceRegistry;
        } else {
            resourceRegistry = new FSRegistry();
        }

        final Store store = this.store.apply(logger);
        final Share share = this.share.apply(logger);
        final TopDownExecutorImpl topDownExecutor =
            new TopDownExecutorImpl(taskDefs, resourceRegistry, store, share, defaultOutputStamper,
                defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger,
                executorLoggerFactory);
        final BottomUpExecutorImpl bottomUpExecutor =
            new BottomUpExecutorImpl(taskDefs, resourceRegistry, store, share, defaultOutputStamper,
                defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger,
                executorLoggerFactory);
        return new PieImpl(topDownExecutor, bottomUpExecutor, taskDefs, resourceRegistry, store, share,
            defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory,
            logger, executorLoggerFactory);
    }
}
