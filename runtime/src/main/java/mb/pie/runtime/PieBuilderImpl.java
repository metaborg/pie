package mb.pie.runtime;

import mb.fs.java.JavaFileSystem;
import mb.pie.api.*;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.fs.FileSystemResourceSystem;
import mb.pie.api.fs.stamp.FileSystemStampers;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.runtime.exec.BottomUpExecutorImpl;
import mb.pie.runtime.exec.TopDownExecutorImpl;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.NoopLogger;
import mb.pie.runtime.logger.exec.LoggerExecutorLogger;
import mb.pie.runtime.resourcesystems.MapResourceSystems;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.function.Function;

public class PieBuilderImpl implements PieBuilder {
    private @Nullable TaskDefs taskDefs = null;
    private @Nullable ResourceSystems resourceSystems = null;
    private Function<Logger, Store> store = (logger) -> new InMemoryStore();
    private Function<Logger, Share> share = (logger) -> new NonSharingShare();
    private OutputStamper defaultOutputStamper = OutputStampers.getEquals();
    private ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper = FileSystemStampers.getModified();
    private ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper = FileSystemStampers.getModified();
    private Function<Logger, Layer> layerFactory = ValidationLayer::new;
    private Logger logger = new NoopLogger();
    private Function<Logger, ExecutorLogger> executorLoggerFactory = LoggerExecutorLogger::new;


    @Override public PieBuilderImpl withTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
        return this;
    }

    @Override public PieBuilder withResourceSystems(ResourceSystems resourceSystems) {
        this.resourceSystems = resourceSystems;
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

    @Override public PieBuilderImpl withDefaultRequireFileSystemStamper(ResourceStamper<FileSystemResource> stamper) {
        this.defaultRequireFileSystemStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withDefaultProvideFileSystemStamper(ResourceStamper<FileSystemResource> stamper) {
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

        final ResourceSystems resourceSystems;
        if(this.resourceSystems != null) {
            resourceSystems = this.resourceSystems;
        } else {
            final HashMap<String, ResourceSystem> map = new HashMap<>();
            map.put(JavaFileSystem.id, new FileSystemResourceSystem(JavaFileSystem.instance));
            resourceSystems = new MapResourceSystems(map);
        }

        final Store store = this.store.apply(logger);
        final Share share = this.share.apply(logger);
        final TopDownExecutorImpl topDownExecutor = new TopDownExecutorImpl(taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger, executorLoggerFactory);
        final BottomUpExecutorImpl bottomUpExecutor = new BottomUpExecutorImpl(taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger, executorLoggerFactory);
        return new PieImpl(topDownExecutor, bottomUpExecutor, taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory, logger, executorLoggerFactory);
    }
}
