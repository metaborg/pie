package mb.pie.bench.state;

import mb.pie.api.ExecException;
import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.bench.util.PieMetricsProfiler;
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
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.Serializable;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@State(Scope.Thread)
public class PieState {
    // Trial

    private @Nullable Pie pie;

    public PieState setupTrial(TaskDefs taskDefs, Pie... ancestors) {
        if(pie != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        final PieBuilderImpl pieBuilder = new PieBuilderImpl();
        pieBuilder.withTaskDefs(taskDefs);
        pieBuilder.withStoreFactory(store.get());
        pieBuilder.withShareFactory(share.get());
        pieBuilder.withDefaultOutputStamper(outputStamper.get());
        pieBuilder.withDefaultRequireReadableResourceStamper(requireResourceStamper.getReadable());
        pieBuilder.withDefaultProvideReadableResourceStamper(provideResourceStamper.getReadable());
        pieBuilder.withDefaultRequireHierarchicalResourceStamper(requireResourceStamper.getHierarchical());
        pieBuilder.withDefaultProvideHierarchicalResourceStamper(provideResourceStamper.getHierarchical());
        pieBuilder.withLayerFactory(layer.get());
        pieBuilder.withLogger(logger.get());
        pieBuilder.withExecutorLoggerFactory(executorLogger.get());
        this.pie = pieBuilder.build().createChildBuilder(ancestors).build();
        return this;
    }

    public PieState setupTrial(Pie... ancestors) {
        return setupTrial(new NullTaskDefs(), ancestors);
    }

    public void tearDownTrial() {
        if(pie == null) {
            throw new IllegalStateException("tearDownTrial was called before calling setupTrial");
        }
    }


    // Invocation

    public void setupInvocation() {
        // Nothing to do yet.
    }

    @SuppressWarnings("ConstantConditions")
    public <O extends @Nullable Serializable> O requireTopDownInNewSession(Task<O> task, String name) throws ExecException, InterruptedException {
        try(final MixedSession session = pie.newSession()) {
            PieMetricsProfiler.getInstance().start();
            final O result = session.require(task);
            PieMetricsProfiler.getInstance().stop(name);
            return result;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void requireBottomUpInNewSession(Set<? extends ResourceKey> changedResources, String name) throws ExecException, InterruptedException {
        try(final MixedSession session = pie.newSession()) {
            PieMetricsProfiler.getInstance().start();
            session.updateAffectedBy(changedResources);
            PieMetricsProfiler.getInstance().stop(name);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void resetState() {
        pie.dropStore();
        pie.dropCallbacks();
    }

    public void tearDownInvocation() {
        resetState();
    }


    @Param({"in_memory"}) public StoreKind store;
    /*@Param({"non_sharing"})*/ public ShareKind share = ShareKind.non_sharing;
    /*@Param({"equals"})*/ private OutputStamperKind outputStamper = OutputStamperKind.equals;
    @Param({"modified"}) private ResourceStamperKind requireResourceStamper;
    @Param({"modified"}) private ResourceStamperKind provideResourceStamper;
    @Param({"validation"}) public LayerKind layer;
    @Param({"stdout_errors"}) public LoggerKind logger;
    @Param({"noop"}) public ExecutorLoggerKind executorLogger;

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

    public enum ResourceStamperKind {
        exists {
            @Override public ResourceStamper<ReadableResource> getReadable() {
                return ResourceStampers.exists();
            }

            @Override public ResourceStamper<HierarchicalResource> getHierarchical() {
                return ResourceStampers.exists();
            }
        }, modified {
            @Override public ResourceStamper<ReadableResource> getReadable() {
                return ResourceStampers.modifiedFile();
            }

            @Override public ResourceStamper<HierarchicalResource> getHierarchical() {
                return ResourceStampers.modifiedFile();
            }
        }, hash {
            @Override public ResourceStamper<ReadableResource> getReadable() {
                return ResourceStampers.hashFile();
            }

            @Override public ResourceStamper<HierarchicalResource> getHierarchical() {
                return ResourceStampers.hashDirRec();
            }
        };

        public abstract ResourceStamper<ReadableResource> getReadable();

        public abstract ResourceStamper<HierarchicalResource> getHierarchical();
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
