package mb.pie.runtime;

import mb.log.api.LoggerFactory;
import mb.pie.api.Callbacks;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.PieBuilder.LayerFactory;
import mb.pie.api.PieChildBuilder;
import mb.pie.api.TaskDefs;
import mb.pie.api.Tracer;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.taskdefs.CompositeTaskDefs;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PieChildBuilderImpl implements PieChildBuilder {
    private final PieImpl parent;
    protected @Nullable TaskDefs taskDefs;
    protected @Nullable ResourceService resourceService;
    protected List<TaskDefs> ancestorTaskDefs;
    protected List<ResourceService> ancestorResourceServices;
    protected List<Callbacks> ancestorCallbacks;
    protected OutputStamper defaultOutputStamper;
    protected ResourceStamper<ReadableResource> defaultRequireReadableStamper;
    protected ResourceStamper<ReadableResource> defaultProvideReadableStamper;
    protected ResourceStamper<HierarchicalResource> defaultRequireHierarchicalStamper;
    protected ResourceStamper<HierarchicalResource> defaultProvideHierarchicalStamper;
    protected LayerFactory layerFactory;
    protected LoggerFactory loggerFactory;
    protected Function<LoggerFactory, Tracer> tracerFactory;

    public PieChildBuilderImpl(PieImpl parent) {
        this.parent = parent;
        this.defaultOutputStamper = parent.defaultStampers.output;
        this.defaultRequireReadableStamper = parent.defaultStampers.requireReadableResource;
        this.defaultProvideReadableStamper = parent.defaultStampers.provideReadableResource;
        this.defaultRequireHierarchicalStamper = parent.defaultStampers.requireHierarchicalResource;
        this.defaultProvideHierarchicalStamper = parent.defaultStampers.provideHierarchicalResource;
        this.layerFactory = parent.layerFactory;
        this.loggerFactory = parent.loggerFactory;
        this.tracerFactory = parent.tracerFactory;
        // Following fields need special handling at build-time.
        this.taskDefs = null;
        this.resourceService = null;
        this.ancestorTaskDefs = new ArrayList<>(Collections.singleton(parent.taskDefs));
        this.ancestorResourceServices = new ArrayList<>(Collections.singleton(parent.resourceService));
        this.ancestorCallbacks = new ArrayList<>(Collections.singleton(parent.callbacks));
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
    public PieChildBuilderImpl withLayerFactory(LayerFactory layerFactory) {
        this.layerFactory = layerFactory;
        return this;
    }

    @Override
    public PieChildBuilderImpl withLoggerFactory(LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
        return this;
    }

    @Override
    public PieChildBuilderImpl withTracerFactory(Function<LoggerFactory, Tracer> tracerFactory) {
        this.tracerFactory = tracerFactory;
        return this;
    }

    @Override
    public PieChildBuilder addTaskDefs(TaskDefs taskDefs) {
        this.ancestorTaskDefs.add(taskDefs);
        return this;
    }

    @Override
    public PieChildBuilder addResourceService(ResourceService resourceService) {
        this.ancestorResourceServices.add(resourceService);
        return this;
    }

    @Override
    public PieChildBuilder addCallBacks(Callbacks callbacks) {
        this.ancestorCallbacks.add(callbacks);
        return this;
    }

    @Override public PieImpl build() {
        final TaskDefs taskDefs;
        if(this.taskDefs != null) {
            taskDefs = new CompositeTaskDefs(ancestorTaskDefs, this.taskDefs);
        } else {
            taskDefs = new CompositeTaskDefs(ancestorTaskDefs, new MapTaskDefs());
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
            resourceService = this.resourceService.createChild(ancestorResourceServices.toArray(new ResourceService[0]));
        } else if(ancestorResourceServices.size() == 1) {
            // Don't create child, but just reuse resourceService from parent
            resourceService = ancestorResourceServices.get(0);
        } else {
            // Class contract guarantees ancestorResourceServices.size() > 1
            resourceService = ancestorResourceServices.get(0)
                .createChild(ancestorResourceServices.stream()
                    .skip(1) // Skip root
                    .toArray(ResourceService[]::new));
        }
        return new PieImpl(
            false,
            taskDefs,
            resourceService,
            parent.serde,
            parent.store,
            parent.share,
            defaultStampers,
            layerFactory,
            loggerFactory,
            tracerFactory,
            new CompositeCallbacks(new MapCallbacks(), ancestorCallbacks)
        );
    }
}
