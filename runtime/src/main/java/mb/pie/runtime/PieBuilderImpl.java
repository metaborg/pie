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
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.NoopLogger;
import mb.pie.runtime.logger.exec.LoggerExecutorLogger;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.taskdefs.NullTaskDefs;
import mb.resource.DefaultResourceService;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.resource.hierarchical.HierarchicalResource;

import java.util.function.BiFunction;
import java.util.function.Function;

public class PieBuilderImpl implements PieBuilder {
    protected TaskDefs taskDefs = new NullTaskDefs();
    protected ResourceService resourceService = new DefaultResourceService(new FSResourceRegistry());
    protected Function<Logger, Store> store = (logger) -> new InMemoryStore();
    protected Function<Logger, Share> share = (logger) -> new NonSharingShare();
    protected OutputStamper defaultOutputStamper = OutputStampers.equals();
    protected ResourceStamper<ReadableResource> defaultRequireReadableStamper = ResourceStampers.modifiedFile();
    protected ResourceStamper<ReadableResource> defaultProvideReadableStamper = ResourceStampers.modifiedFile();
    protected ResourceStamper<HierarchicalResource> defaultRequireHierarchicalStamper = ResourceStampers.modifiedFile();
    protected ResourceStamper<HierarchicalResource> defaultProvideHierarchicalStamper = ResourceStampers.modifiedFile();
    protected BiFunction<TaskDefs, Logger, Layer> layer = ValidationLayer::new;
    protected Logger logger = new NoopLogger();
    protected Function<Logger, ExecutorLogger> executorLoggerFactory = LoggerExecutorLogger::new;

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

    @Override public PieBuilderImpl withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultRequireHierarchicalStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultProvideHierarchicalStamper = stamper;
        return this;
    }

    @Override public PieBuilderImpl withLayer(BiFunction<TaskDefs, Logger, Layer> layer) {
        this.layer = layer;
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
                defaultRequireHierarchicalStamper, defaultProvideHierarchicalStamper);
        return new PieImpl(taskDefs, resourceService, store, share, defaultStampers, layer, logger,
            executorLoggerFactory);
    }
}
