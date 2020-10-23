package mb.pie.bench.state;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.layer.NoopLayer;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.logger.NoopLogger;
import mb.pie.runtime.logger.StreamLogger;
import mb.pie.runtime.logger.exec.LoggerExecutorLogger;
import mb.pie.runtime.logger.exec.NoopExecutorLogger;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.taskdefs.NullTaskDefs;
import mb.resource.ReadableResource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.function.BiFunction;
import java.util.function.Function;

@State(Scope.Thread)
public class PieState {
    private Pie pie;

    public PieState setup(TaskDefs taskDefs, Pie... ancestors) {
        final PieBuilderImpl pieBuilder = new PieBuilderImpl();
        pieBuilder.withTaskDefs(taskDefs);
        pieBuilder.withStoreFactory(storeKind.get());
        pieBuilder.withShareFactory(shareKind.get());
        pieBuilder.withDefaultOutputStamper(defaultOutputStamperKind.get());
        pieBuilder.withDefaultRequireReadableResourceStamper(defaultRequireReadableResourceStamperKind.get());
        pieBuilder.withDefaultProvideReadableResourceStamper(defaultProvideReadableResourceStamperKind.get());
        pieBuilder.withDefaultRequireHierarchicalResourceStamper(defaultRequireHierarchicalResourceStamperKind.get());
        pieBuilder.withDefaultProvideHierarchicalResourceStamper(defaultProvideHierarchicalResourceStamperKind.get());
        pieBuilder.withLayerFactory(layerKind.get());
        pieBuilder.withLogger(loggerKind.get());
        pieBuilder.withExecutorLoggerFactory(executorLoggerKind.get());
        this.pie = pieBuilder.build().createChildBuilder(ancestors).build();
        return this;
    }

    public PieState setup(Pie... ancestors) {
        return setup(new NullTaskDefs(), ancestors);
    }

    public void reset() {
        pie.dropStore();
        pie.dropCallbacks();
    }

    public Pie getPie() {
        return pie;
    }

    public MixedSession newSession() {
        return pie.newSession();
    }

    @Param({"in_memory"}) public StoreKind storeKind;
    @Param({"non_sharing"}) public ShareKind shareKind;
    @Param({"equals"}) private OutputStamperKind defaultOutputStamperKind;
    @Param({"modified"}) private ReadableResourceStamperKind defaultRequireReadableResourceStamperKind;
    @Param({"modified"}) private ReadableResourceStamperKind defaultProvideReadableResourceStamperKind;
    @Param({"modified_direct"}) private HierarchicalResourceStamperKind defaultRequireHierarchicalResourceStamperKind;
    @Param({"modified_direct"}) private HierarchicalResourceStamperKind defaultProvideHierarchicalResourceStamperKind;
    @Param({"validation"}) public LayerKind layerKind;
    @Param({"stdout_errors"}) public LoggerKind loggerKind;
    @Param({"logger"}) public ExecutorLoggerKind executorLoggerKind;

    public enum StoreKind {
        in_memory {
            @Override public BiFunction<Logger, ResourceService, Store> get() {
                return (logger, resourceService) -> new InMemoryStore();
            }
        };

        public abstract BiFunction<Logger, ResourceService, Store> get();
    }

    public enum ShareKind {
        non_sharing {
            @Override public Function<Logger, Share> get() {
                return (logger) -> new NonSharingShare();
            }
        };

        public abstract Function<Logger, Share> get();
    }

    public enum OutputStamperKind {
        equals {
            @Override public OutputStamper get() {
                return OutputStampers.equals();
            }
        },
        inconsequential {
            @Override public OutputStamper get() {
                return OutputStampers.inconsequential();
            }
        };

        public abstract OutputStamper get();
    }

    public enum ReadableResourceStamperKind {
        exists {
            @Override public ResourceStamper<ReadableResource> get() { return ResourceStampers.exists(); }
        }, modified {
            @Override public ResourceStamper<ReadableResource> get() { return ResourceStampers.modifiedFile(); }
        }, hash {
            @Override public ResourceStamper<ReadableResource> get() { return ResourceStampers.hashFile(); }
        };

        public abstract ResourceStamper<ReadableResource> get();
    }

    public enum HierarchicalResourceStamperKind {
        exists {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.exists(); }
        }, modified_direct {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.modifiedFile(); }
        }, modified_one_level {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.modifiedDir(); }
        }, modified_recursive {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.modifiedDirRec(); }
        }, hash_file_only {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.hashFile(); }
        }, hash_one_level {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.hashDir(); }
        }, hash_recursive {
            @Override public ResourceStamper<HierarchicalResource> get() { return ResourceStampers.hashDirRec(); }
        };

        public abstract ResourceStamper<HierarchicalResource> get();
    }

    public enum LayerKind {
        validation {
            @Override public BiFunction<TaskDefs, Logger, Layer> get() {
                return ValidationLayer::new;
            }
        },
        noop {
            @Override public BiFunction<TaskDefs, Logger, Layer> get() {
                return (taskDefs, logger) -> new NoopLayer();
            }
        };

        public abstract BiFunction<TaskDefs, Logger, Layer> get();
    }

    public enum LoggerKind {
        stdout_errors {
            @Override public Logger get() {
                return StreamLogger.onlyErrors();
            }
        },
        stdout_non_verbose {
            @Override public Logger get() {
                return StreamLogger.nonVerbose();
            }
        },
        stdout_verbose {
            @Override public Logger get() {
                return StreamLogger.verbose();
            }
        },
        noop {
            @Override public Logger get() {
                return new NoopLogger();
            }
        };

        public abstract Logger get();
    }

    public enum ExecutorLoggerKind {
        logger {
            @Override public Function<Logger, ExecutorLogger> get() {
                return LoggerExecutorLogger::new;
            }
        },
        noop {
            @Override public Function<Logger, ExecutorLogger> get() {
                return (logger) -> new NoopExecutorLogger();
            }
        };

        public abstract Function<Logger, ExecutorLogger> get();
    }
}
