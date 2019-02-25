package mb.pie.api;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;

import java.util.function.Function;

/**
 * Builder for [PIE][Pie] facade.
 */
public interface PieBuilder {
    PieBuilder withTaskDefs(TaskDefs taskDefs);

    PieBuilder withResourceSystems(ResourceSystems resourceSystems);

    PieBuilder withStore(Function<Logger, Store> storeFunc);

    PieBuilder withShare(Function<Logger, Share> shareFunc);

    PieBuilder withDefaultOutputStamper(OutputStamper outputStamper);

    PieBuilder withDefaultRequireFileSystemStamper(ResourceStamper stamper);

    PieBuilder withDefaultProvideFileSystemStamper(ResourceStamper stamper);

    PieBuilder withLayer(Function<Logger, Layer> layerFunc);

    PieBuilder withLogger(Logger logger);

    PieBuilder withExecutorLogger(Function<Logger, ExecutorLogger> execLoggerFunc);


    Pie build();
}
