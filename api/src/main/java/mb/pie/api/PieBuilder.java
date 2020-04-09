package mb.pie.api;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Builder for a {@link Pie} entry point.
 */
public interface PieBuilder {
    PieBuilder withTaskDefs(TaskDefs taskDefs);

    PieBuilder withResourceService(ResourceService resourceService);

    PieBuilder withStoreFactory(Function<Logger, Store> storeFunc);

    PieBuilder withShareFactory(Function<Logger, Share> shareFunc);

    PieBuilder withDefaultOutputStamper(OutputStamper outputStamper);

    PieBuilder withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieBuilder withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieBuilder withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieBuilder withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieBuilder withLayerFactory(BiFunction<TaskDefs, Logger, Layer> layerFunc);

    PieBuilder withLogger(Logger logger);

    PieBuilder withExecutorLoggerFactory(Function<Logger, ExecutorLogger> execLoggerFunc);


    Pie build();
}
