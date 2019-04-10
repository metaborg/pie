package mb.pie.runtime.exec;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.exec.TopDownExecutor;
import mb.pie.api.exec.TopDownSession;
import mb.pie.runtime.DefaultStampers;
import mb.resource.ResourceRegistry;

import java.util.function.Function;

public class TopDownExecutorImpl implements TopDownExecutor {
    private final TaskDefs taskDefs;
    private final ResourceRegistry resourceRegistry;
    private final Store store;
    private final Share share;
    private final DefaultStampers defaultStampers;
    private final Function<Logger, Layer> layerFactory;
    private final Logger logger;
    private final Function<Logger, ExecutorLogger> executorLoggerFactory;

    public TopDownExecutorImpl(
        TaskDefs taskDefs,
        ResourceRegistry resourceRegistry,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        Function<Logger, Layer> layerFactory,
        Logger logger,
        Function<Logger, ExecutorLogger> executorLoggerFactory
    ) {
        this.taskDefs = taskDefs;
        this.resourceRegistry = resourceRegistry;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layerFactory = layerFactory;
        this.logger = logger;
        this.executorLoggerFactory = executorLoggerFactory;
    }

    @Override public TopDownSession newSession() {
        return new TopDownSessionImpl(taskDefs, resourceRegistry, store, share, defaultStampers,
            layerFactory.apply(logger), logger, executorLoggerFactory.apply(logger));
    }
}
