package mb.pie.runtime;

import mb.pie.api.*;
import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.exec.TopDownExecutor;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;

import java.util.function.Function;

public class PieImpl implements Pie {
    private final TopDownExecutor topDownExecutor;
    private final BottomUpExecutor bottomUpExecutor;
    final TaskDefs taskDefs;
    final ResourceSystems resourceSystems;
    final Store store;
    final Share share;
    final OutputStamper defaultOutputStamper;
    final ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper;
    final ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper;
    final Function<Logger, Layer> layerFactory;
    final Logger logger;
    final Function<Logger, ExecutorLogger> executorLoggerFactory;

    public PieImpl(
        TopDownExecutor topDownExecutor,
        BottomUpExecutor bottomUpExecutor,
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
        this.topDownExecutor = topDownExecutor;
        this.bottomUpExecutor = bottomUpExecutor;
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
