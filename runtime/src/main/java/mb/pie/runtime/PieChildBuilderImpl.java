package mb.pie.runtime;

import mb.pie.api.Callbacks;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PieChildBuilderImpl implements PieChildBuilder {
    private final PieImpl parent;
    protected List<TaskDefs> taskDefs;
    protected List<ResourceService> resourceServices;
    protected List<Callbacks> callbacks;
    protected OutputStamper defaultOutputStamper;
    protected ResourceStamper<ReadableResource> defaultRequireReadableStamper;
    protected ResourceStamper<ReadableResource> defaultProvideReadableStamper;
    protected ResourceStamper<HierarchicalResource> defaultRequireHierarchicalStamper;
    protected ResourceStamper<HierarchicalResource> defaultProvideHierarchicalStamper;
    protected BiFunction<TaskDefs, Logger, Layer> layerFactory;
    protected Logger logger;
    protected Function<Logger, ExecutorLogger> executorLoggerFactory;

    public PieChildBuilderImpl(PieImpl parent) {
        this.parent = parent;
        this.defaultOutputStamper = parent.defaultStampers.output;
        this.defaultRequireReadableStamper = parent.defaultStampers.requireReadableResource;
        this.defaultProvideReadableStamper = parent.defaultStampers.provideReadableResource;
        this.defaultRequireHierarchicalStamper = parent.defaultStampers.requireHierarchicalResource;
        this.defaultProvideHierarchicalStamper = parent.defaultStampers.provideHierarchicalResource;
        this.layerFactory = parent.layerFactory;
        this.logger = parent.logger;
        this.executorLoggerFactory = parent.executorLoggerFactory;
        // Following fields need special handling at build-time.
        this.taskDefs = new ArrayList<>(Collections.singleton(parent.taskDefs));
        this.resourceServices = new ArrayList<>(Collections.singleton(parent.resourceService));
        this.callbacks = new ArrayList<>(Collections.singleton(parent.callbacks));
    }


    @Override
    public PieChildBuilderImpl withTaskDefs(TaskDefs taskDefs) {
        this.taskDefs.clear();
        this.taskDefs.add(taskDefs);
        return this;
    }

    @Override
    public PieChildBuilderImpl withResourceService(ResourceService resourceService) {
        this.resourceServices.clear();
        this.resourceServices.add(resourceService);
        return this;
    }

    @Override
    public PieChildBuilder withCallbacks(Callbacks callbacks) {
        this.callbacks.clear();
        this.callbacks.add(callbacks);
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

    @Override
    public PieChildBuilder addTaskDefs(TaskDefs taskDefs) {
        this.taskDefs.add(taskDefs);
        return this;
    }

    @Override
    public PieChildBuilder addResourceService(ResourceService resourceService) {
        this.resourceServices.add(resourceService);
        return this;
    }

    @Override
    public PieChildBuilder addCallBacks(Callbacks callbacks) {
        this.callbacks.add(callbacks);
        return this;
    }

    @Override public PieImpl build() {
        final TaskDefs taskDefs = this.taskDefs
            .stream()
            .reduce(CompositeTaskDefs::new)
            .get(); // Safe, because constructor enforces that there is at least one taskDefs instance
        final DefaultStampers defaultStampers = new DefaultStampers(
            defaultOutputStamper,
            defaultRequireReadableStamper,
            defaultProvideReadableStamper,
            defaultRequireHierarchicalStamper,
            defaultProvideHierarchicalStamper
        );
        final ResourceService resourceService;
        if (resourceServices.size() == 1) {
            // Dont create child, but just reuse resourceService form parent
            resourceService = resourceServices.get(0);
        } else {
            // Class contract guarantees resourceServices.size() > 1
            resourceService = resourceServices.get(0).createChild(resourceServices.stream()
                .skip(1) // Skip root
                .toArray(ResourceService[]::new));
        }
        final Callbacks parentsCallbacks = callbacks.stream()
            .reduce(CompositeCallbacks::new)
            .get(); // Safe, because constructor enforces that there is at least one callbacks instance
        return new PieImpl(
            taskDefs,
            resourceService,
            parent.store,
            parent.share,
            defaultStampers,
            layerFactory,
            logger,
            executorLoggerFactory,
            new CompositeCallbacks(new MapCallbacks(), parentsCallbacks)
        );
    }
}
