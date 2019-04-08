package mb.pie.runtime.exec;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.exec.TopDownExecutor;
import mb.pie.api.exec.TopDownSession;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSResource;

import java.util.function.Function;

public class TopDownExecutorImpl implements TopDownExecutor {
    private final TaskDefs taskDefs;
    private final ResourceRegistry resourceRegistry;
    private final Store store;
    private final Share share;
    private final OutputStamper defaultOutputStamper;
    private final ResourceStamper<FSResource> defaultRequireFileSystemStamper;
    private final ResourceStamper<FSResource> defaultProvideFileSystemStamper;
    private final Function<Logger, Layer> layerFactory;
    private final Logger logger;
    private final Function<Logger, ExecutorLogger> executorLoggerFactory;

    public TopDownExecutorImpl(
        TaskDefs taskDefs,
        ResourceRegistry resourceRegistry,
        Store store,
        Share share,
        OutputStamper defaultOutputStamper,
        ResourceStamper<FSResource> defaultRequireFileSystemStamper,
        ResourceStamper<FSResource> defaultProvideFileSystemStamper,
        Function<Logger, Layer> layerFactory,
        Logger logger,
        Function<Logger, ExecutorLogger> executorLoggerFactory
    ) {
        this.taskDefs = taskDefs;
        this.resourceRegistry = resourceRegistry;
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
        return new TopDownSessionImpl(taskDefs, resourceRegistry, store, share, defaultOutputStamper,
            defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layerFactory.apply(logger), logger,
            executorLoggerFactory.apply(logger));
    }
}
