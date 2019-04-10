package mb.pie.api;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSResource;

import java.util.function.Function;

/**
 * Builder for [PIE][Pie] facade.
 */
public interface PieBuilder {
    PieBuilder withTaskDefs(TaskDefs taskDefs);

    PieBuilder withResourceRegistry(ResourceRegistry resourceRegistry);

    PieBuilder withStore(Function<Logger, Store> storeFunc);

    PieBuilder withShare(Function<Logger, Share> shareFunc);

    PieBuilder withDefaultOutputStamper(OutputStamper outputStamper);

    PieBuilder withDefaultRequireReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieBuilder withDefaultProvideReadableResourceStamper(ResourceStamper<ReadableResource> stamper);

    PieBuilder withDefaultRequireFSResourceStamper(ResourceStamper<FSResource> stamper);

    PieBuilder withDefaultProvideFSResourceStamper(ResourceStamper<FSResource> stamper);

    PieBuilder withLayer(Function<Logger, Layer> layerFunc);

    PieBuilder withLogger(Logger logger);

    PieBuilder withExecutorLogger(Function<Logger, ExecutorLogger> execLoggerFunc);


    Pie build();
}
