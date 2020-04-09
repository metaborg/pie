package mb.pie.dagger;

import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@Module public abstract class PieOptionalsModule {
    @Provides @Singleton @ElementsIntoSet
    static Set<TaskDef<?, ?>> provideTaskDefs() {
        return new HashSet<>();
    }

    @BindsOptionalOf
    abstract ResourceService resourceService();

    @BindsOptionalOf
    abstract Function<Logger, Store> storeFunc();

    @BindsOptionalOf
    abstract Function<Logger, Share> shareFunc();

    @BindsOptionalOf
    abstract OutputStamper defaultOutputStamper();

    @BindsOptionalOf @Named("require")
    abstract ResourceStamper<ReadableResource> defaultRequireReadableResourceStamper();

    @BindsOptionalOf @Named("provide")
    abstract ResourceStamper<ReadableResource> defaultProvideReadableResourceStamper();

    @BindsOptionalOf @Named("require")
    abstract ResourceStamper<HierarchicalResource> defaultRequireHierarchicalResourceStamper();

    @BindsOptionalOf @Named("provide")
    abstract ResourceStamper<HierarchicalResource> defaultProvideHierarchicalResourceStamper();

    @BindsOptionalOf
    abstract BiFunction<TaskDefs, Logger, Layer> layerFunc();

    @BindsOptionalOf
    abstract Logger logger();

    @BindsOptionalOf
    abstract Function<Logger, ExecutorLogger> execLoggerFunc();
}
