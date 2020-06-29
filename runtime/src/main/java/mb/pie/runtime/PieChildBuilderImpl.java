package mb.pie.runtime;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.PieChildBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.taskdefs.CompositeTaskDefs;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PieChildBuilderImpl implements PieChildBuilder {
    private final List<PieImpl> ancestors;
    protected @Nullable TaskDefs taskDefs;
    protected @Nullable ResourceService resourceService;
    protected OutputStamper defaultOutputStamper;
    protected ResourceStamper<ReadableResource> defaultRequireReadableStamper;
    protected ResourceStamper<ReadableResource> defaultProvideReadableStamper;
    protected ResourceStamper<HierarchicalResource> defaultRequireHierarchicalStamper;
    protected ResourceStamper<HierarchicalResource> defaultProvideHierarchicalStamper;
    protected BiFunction<TaskDefs, Logger, Layer> layerFactory;
    protected Logger logger;
    protected Function<Logger, ExecutorLogger> executorLoggerFactory;

    // Field that can not be mutated by builder methods
    private final Store store;
    private final Share share;


    public PieChildBuilderImpl(PieImpl parent, PieImpl... ancestors) {
        this.ancestors = new ArrayList<>();
        this.ancestors.add(parent);
        this.ancestors.addAll(Arrays.asList(ancestors));
        this.defaultOutputStamper = parent.defaultStampers.output;
        this.defaultRequireReadableStamper = parent.defaultStampers.requireReadableResource;
        this.defaultProvideReadableStamper = parent.defaultStampers.provideReadableResource;
        this.defaultRequireHierarchicalStamper = parent.defaultStampers.requireHierarchicalResource;
        this.defaultProvideHierarchicalStamper = parent.defaultStampers.provideHierarchicalResource;
        this.layerFactory = parent.layerFactory;
        this.logger = parent.logger;
        this.executorLoggerFactory = parent.executorLoggerFactory;
        this.store = parent.store;
        this.share = parent.share;
        // Following fields need special handling at build-time.
        this.taskDefs = null;
        this.resourceService = null;
    }


    @Override
    public PieChildBuilderImpl withTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = taskDefs;
        return this;
    }

    @Override
    public PieChildBuilderImpl withResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
        return this;
    }

    @Override
    public PieChildBuilderImpl withDefaultOutputStamper(OutputStamper stamper) {
        this.defaultOutputStamper = stamper;
        return this;
    }

    @Override
    public PieChildBuilderImpl withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultRequireReadableStamper = stamper;
        return this;
    }

    @Override
    public PieChildBuilderImpl withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper) {
        this.defaultProvideReadableStamper = stamper;
        return this;
    }

    @Override
    public PieChildBuilderImpl withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultRequireHierarchicalStamper = stamper;
        return this;
    }

    @Override
    public PieChildBuilderImpl withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper) {
        this.defaultProvideHierarchicalStamper = stamper;
        return this;
    }

    @Override
    public PieChildBuilderImpl withLayerFactory(BiFunction<TaskDefs, Logger, Layer> layer) {
        this.layerFactory = layer;
        return this;
    }

    @Override
    public PieChildBuilderImpl withLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    @Override
    public PieChildBuilderImpl withExecutorLoggerFactory(Function<Logger, ExecutorLogger> executorLogger) {
        this.executorLoggerFactory = executorLogger;
        return this;
    }

    @Override public PieImpl build() {
        final TaskDefs parentsTaskDefs = ancestors.stream()
            .map(PieImpl::getTaskDefs)
            .reduce(CompositeTaskDefs::new)
            .get(); // Safe, because constructor enforces that there is at least one ancestor
        final TaskDefs taskDefs;
        if(this.taskDefs != null) {
            taskDefs = new CompositeTaskDefs(parentsTaskDefs, this.taskDefs);
        } else {
            taskDefs = parentsTaskDefs;
        }
        final DefaultStampers defaultStampers = new DefaultStampers(
            defaultOutputStamper,
            defaultRequireReadableStamper,
            defaultProvideReadableStamper,
            defaultRequireHierarchicalStamper,
            defaultProvideHierarchicalStamper
        );
        final ResourceService resourceService;
        if(this.resourceService != null) {
            // Dont instantiate
            resourceService = this.resourceService.createChild(ancestors.stream()
                .map(PieImpl::getResourceService)
                .toArray(ResourceService[]::new));
        } else if (ancestors.size() == 1) {
            resourceService = ancestors.get(0).resourceService;
        } else {
            resourceService = ancestors.get(0).resourceService.createChild(ancestors.stream()
                .skip(1) // Skip root
                .map(PieImpl::getResourceService)
                .toArray(ResourceService[]::new));
        }
        final Callbacks parentsCallbacks = ancestors.stream()
            .map(PieImpl::getCallbacks)
            .reduce(CompositeCallbacks::new)
            .get(); // Safe, because constructor enforces that there is at least one ancestor
        return new PieImpl(
            taskDefs,
            resourceService,
            store,
            share,
            defaultStampers,
            layerFactory,
            logger,
            executorLoggerFactory,
            new CompositeCallbacks(new MapCallbacks(), parentsCallbacks)
        );
    }
}
