package mb.pie.api;

import mb.log.api.LoggerFactory;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Builder for a child {@link Pie} entry point.
 */
public interface PieChildBuilder {
    PieChildBuilder withTaskDefs(TaskDefs taskDefs);

    PieChildBuilder withResourceService(ResourceService resourceService);

    PieChildBuilder withDefaultOutputStamper(OutputStamper outputStamper);

    PieChildBuilder withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieChildBuilder withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieChildBuilder withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieChildBuilder withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieChildBuilder withLayerFactory(BiFunction<TaskDefs, LoggerFactory, Layer> layerFactory);

    PieChildBuilder withLoggerFactory(LoggerFactory loggerFactory);

    PieChildBuilder withTracerFactory(Function<LoggerFactory, Tracer> tracerFactory);

    PieChildBuilder addTaskDefs(TaskDefs taskDefs);

    PieChildBuilder addResourceService(ResourceService resourceService);

    PieChildBuilder addCallBacks(Callbacks callbacks);


    Pie build();
}
