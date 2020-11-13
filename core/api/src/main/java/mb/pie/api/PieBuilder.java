package mb.pie.api;

import mb.log.api.LoggerFactory;
import mb.pie.api.serde.Serde;
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

    PieBuilder withSerdeFactory(Function<LoggerFactory, Serde> serdeFactory);

    @FunctionalInterface interface StoreFactory {
        Store apply(Serde serde, ResourceService resourceService, LoggerFactory loggerFactory);
    }

    PieBuilder withStoreFactory(StoreFactory storeFactory);

    PieBuilder withShareFactory(Function<LoggerFactory, Share> shareFactory);

    PieBuilder withDefaultOutputStamper(OutputStamper outputStamper);

    PieBuilder withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieBuilder withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieBuilder withDefaultRequireHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieBuilder withDefaultProvideHierarchicalResourceStamper(ResourceStamper<HierarchicalResource> stamper);

    PieBuilder withLayerFactory(BiFunction<TaskDefs, LoggerFactory, Layer> layerFactory);

    PieBuilder withLoggerFactory(LoggerFactory loggerFactory);

    PieBuilder withTracerFactory(Function<LoggerFactory, Tracer> tracerFactory);


    Pie build();
}
