package mb.pie.runtime;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.Pie;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.TaskDefs;
import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.exec.TopDownExecutor;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSResource;

import java.util.function.Function;

public class PieImpl implements Pie {
    private final TopDownExecutor topDownExecutor;
    private final BottomUpExecutor bottomUpExecutor;
    final TaskDefs taskDefs;
    final ResourceRegistry resourceRegistry;
    final Store store;
    final Share share;
    final OutputStamper defaultOutputStamper;
    final ResourceStamper<FSResource> defaultRequireFileSystemStamper;
    final ResourceStamper<FSResource> defaultProvideFileSystemStamper;
    final Function<Logger, Layer> layerFactory;
    final Logger logger;
    final Function<Logger, ExecutorLogger> executorLoggerFactory;

    public PieImpl(
        TopDownExecutor topDownExecutor,
        BottomUpExecutor bottomUpExecutor,
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
        this.topDownExecutor = topDownExecutor;
        this.bottomUpExecutor = bottomUpExecutor;
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

    @Override public TopDownExecutor getTopDownExecutor() {
        return topDownExecutor;
    }

    @Override public BottomUpExecutor getBottomUpExecutor() {
        return bottomUpExecutor;
    }

    @Override public void dropStore() {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.drop();
        }
    }

    @Override public void close() throws Exception {
        store.close();
    }

    @Override public String toString() {
        return "PieImpl(" + store + ", " + share + ", " + defaultOutputStamper + ", " + defaultRequireFileSystemStamper + ", " + defaultProvideFileSystemStamper + ", " + layerFactory.apply(logger) + ")";
    }
}
