package mb.pie.api;

import mb.log.api.LoggerFactory;
import mb.pie.api.PieBuilder.LayerFactory;
import mb.pie.api.serde.Serde;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import java.util.function.Function;

/**
 * Builder for a child {@link Pie} entry point based on a parent or multiple ancestor {@link Pie} instances.
 */
public interface PieChildBuilder {
    /**
     * Sets the base {@link TaskDefs} to use. The task definitions of ancestors will be composed with this base. By
     * default, this is set to the task definitions of the parent.
     */
    PieChildBuilder withTaskDefs(TaskDefs taskDefs);

    /**
     * Adds a {@link TaskDefs} that will be composed with the base task definitions.
     */
    PieChildBuilder addTaskDefs(TaskDefs taskDefs);

    /**
     * Sets the base {@link ResourceService} to use. The resource services of ancestors will be composed with this base.
     * By default, this is set to the task definition of the parent.
     */
    PieChildBuilder withResourceService(ResourceService resourceService);

    /**
     * Adds a {@link ResourceService} that will be composed with the base resource service.
     */
    PieChildBuilder addResourceService(ResourceService resourceService);


    /**
     * Overrides the {@link Serde serialization and deseserialization implementation} in the child {@link Pie}
     * instance.
     */
    PieChildBuilder overrideSerdeFactory(Function<LoggerFactory, Serde> serdeFactory);

    /**
     * Overrides the {@link Store} in the child {@link Pie} instance, such that it has its own independent store from
     * the parent.
     */
    PieChildBuilder overrideStoreFactory(PieBuilder.StoreFactory storeFactory);

    /**
     * Overrides the {@link Store} in the child {@link Pie} instance with a default one, such that it has its own
     * independent store from the parent.
     */
    PieChildBuilder overrideWithDefaultStoreFactory();

    /**
     * Overrides the {@link Share} in the child {@link Pie} instance.
     */
    PieChildBuilder overrideShareFactory(Function<LoggerFactory, Share> shareFactory);


    PieChildBuilder withDefaultOutputStamper(OutputStamper outputStamper);

    PieChildBuilder withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieChildBuilder withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieChildBuilder withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieChildBuilder withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieChildBuilder withLayerFactory(LayerFactory layerFactory);

    PieChildBuilder withLoggerFactory(LoggerFactory loggerFactory);

    PieChildBuilder withTracerFactory(Function<LoggerFactory, Tracer> tracerFactory);


    /**
     * Adds a {@link Callbacks} that will be composed with the base callbacks. Normally only called internally by {@link
     * Pie} implementations.
     */
    PieChildBuilder addCallBacks(Callbacks callbacks);


    Pie build();
}
