package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.TopDownExecutor;
import mb.pie.api.exec.TopDownSession;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;

import java.util.function.Function;

public class TopDownExecutorImpl implements TopDownExecutor {
    private final TaskDefs taskDefs;
    private final ResourceSystems resourceSystems;
    private final Store store;
    private final Share share;
    private final OutputStamper defaultOutputStamper;
    private final ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper;
    private final ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper;
    private final Function<Logger, Layer> layerFactory;
    private final Logger logger;
    private final Function<Logger, ExecutorLogger> executorLoggerFactory;

    public TopDownExecutorImpl(
        TaskDefs taskDefs,
        ResourceSystems resourceSystems,
        Store store,
        Share share,
        OutputStamper defaultOutputStamper,
        ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper,
        ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper,
        Function<Logger, Layer> layerFactory,
        Logger logger,
        Function<Logger, ExecutorLogger> executorLoggerFactory
    ) {
        this.taskDefs = taskDefs;
        this.resourceSystems = resourceSystems;
        this.store = store;
        this.share = share;
        this.defaultOutputStamper = defaultOutputStamper;
        this.defaultRequireFileSystemStamper = defaultRequireFileSystemStamper;
        this.defaultProvideFileSystemStamper = defaultProvideFileSystemStamper;
        this.layerFactory = layerFactory;
        this.logger = logger;
        this.executorLoggerFactory = executorLoggerFactory;
    }

    @Override public TopDownSession newSession() {
        return new TopDownSessionImpl(taskDefs, resourceSystems, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory.apply(logger), logger, executorLoggerFactory.apply(logger));
    }
}
