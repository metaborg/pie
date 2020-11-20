package mb.pie.bench.state;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.ExecException;
import mb.pie.api.Layer;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder;
import mb.pie.api.PieBuilder.LayerFactory;
import mb.pie.api.Share;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.Tracer;
import mb.pie.api.serde.JavaSerde;
import mb.pie.api.serde.Serde;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.bench.util.PieMetricsProfiler;
import mb.pie.runtime.PieBuilderImpl;
import mb.pie.runtime.layer.NoopLayer;
import mb.pie.runtime.layer.ValidationLayer;
import mb.pie.runtime.share.NonSharingShare;
import mb.pie.runtime.store.InMemoryStore;
import mb.pie.runtime.taskdefs.NullTaskDefs;
import mb.pie.runtime.tracer.CompositeTracer;
import mb.pie.runtime.tracer.LoggingTracer;
import mb.pie.runtime.tracer.MetricsTracer;
import mb.pie.runtime.tracer.NoopTracer;
import mb.pie.serde.kryo.KryoSerde;
import mb.pie.store.lmdb.LMDBStore;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@State(Scope.Thread)
public class PieState {
    // Trial

    private @Nullable LoggerFactory loggerFactory;
    private @Nullable Logger logger;
    private @Nullable HierarchicalResource temporaryDirectory;
    private @Nullable Pie pie;
    private @Nullable MetricsTracer metricsTracer;

    public PieState setupTrial(LoggerFactory loggerFactory, HierarchicalResource temporaryDirectory, TaskDefs taskDefs, Pie... ancestors) {
        if(this.loggerFactory != null || this.temporaryDirectory != null || logger != null || pie != null) {
            throw new IllegalStateException("setupTrial was called before tearDownTrial");
        }
        this.loggerFactory = loggerFactory;
        logger = loggerFactory.create(PieState.class);
        logger.trace("PieState.setupTrial");
        this.temporaryDirectory = temporaryDirectory;
        final PieBuilderImpl pieBuilder = new PieBuilderImpl();
        pieBuilder.withTaskDefs(taskDefs);
        pieBuilder.withSerdeFactory(serde.get());
        pieBuilder.withStoreFactory(store.get(temporaryDirectory));
        pieBuilder.withShareFactory(share.get());
        pieBuilder.withDefaultOutputStamper(outputStamper.get());
        pieBuilder.withDefaultRequireReadableResourceStamper(requireResourceStamper.getReadable());
        pieBuilder.withDefaultProvideReadableResourceStamper(provideResourceStamper.getReadable());
        pieBuilder.withDefaultRequireHierarchicalResourceStamper(requireResourceStamper.getHierarchical());
        pieBuilder.withDefaultProvideHierarchicalResourceStamper(provideResourceStamper.getHierarchical());
        pieBuilder.withLayerFactory(layer.get());
        pieBuilder.withLoggerFactory(loggerFactory);
        pieBuilder.withTracerFactory(tracer.get());
        metricsTracer = tracer.getMetricsTracer();
        pie = pieBuilder.build().createChildBuilder(ancestors).build();
        return this;
    }

    public PieState setupTrial(LoggerFactory loggerFactory, HierarchicalResource temporaryDirectory, Pie... ancestors) {
        return setupTrial(loggerFactory, temporaryDirectory, new NullTaskDefs(), ancestors);
    }

    public void tearDownTrial() {
        if(loggerFactory == null || temporaryDirectory == null || logger == null || pie == null) {
            throw new IllegalStateException("tearDownTrial was called before calling setupTrial");
        }
        metricsTracer = null;
        pie.close();
        pie = null;
        temporaryDirectory = null;
        logger.trace("PieState.tearDownTrial");
        logger = null;
        loggerFactory = null;
    }


    // Invocation

    public void setupInvocation() {
        if(logger == null || pie == null) {
            throw new IllegalStateException("setupInvocation was called before calling setupTrial");
        }
        logger.trace("PieState.setupInvocation");
        resetState(); // Reset state in case the previous run was terminated.
    }

    @SuppressWarnings("ConstantConditions")
    public <O extends @Nullable Serializable> O requireTopDownInNewSession(Task<O> task, String name) throws ExecException, InterruptedException {
        try(final MixedSession session = pie.newSession()) {
            PieMetricsProfiler.getInstance(loggerFactory, metricsTracer).start(name);
            final O result = session.require(task);
            PieMetricsProfiler.getInstance(loggerFactory, metricsTracer).stop(name);
            return result;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void requireBottomUpInNewSession(Set<? extends ResourceKey> changedResources, String name) throws ExecException, InterruptedException {
        try(final MixedSession session = pie.newSession()) {
            PieMetricsProfiler.getInstance(loggerFactory, metricsTracer).start(name);
            session.updateAffectedBy(changedResources);
            PieMetricsProfiler.getInstance(loggerFactory, metricsTracer).stop(name);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void resetState() {
        pie.dropStore();
        pie.dropCallbacks();
    }

    public void tearDownInvocation() {
        resetState();
        logger.trace("PieState.tearDownInvocation");
    }


    @Param({"java"}) public SerdeKind serde;
    @Param({"in_memory"}) public StoreKind store;
    /*@Param({"non_sharing"})*/ public ShareKind share = ShareKind.non_sharing;
    /*@Param({"equals"})*/ private OutputStamperKind outputStamper = OutputStamperKind.equals;
    @Param({"modified"}) private ResourceStamperKind requireResourceStamper;
    @Param({"modified"}) private ResourceStamperKind provideResourceStamper;
    @Param({"validation"}) public LayerKind layer;
    @Param({"metrics"}) public TracerKind tracer;

    public enum SerdeKind {
        java {
            @Override public Function<LoggerFactory, Serde> get() {
                return (loggerFactory) -> new JavaSerde(PieState.class.getClassLoader());
            }
        },
        kryo {
            @Override public Function<LoggerFactory, Serde> get() {
                return (loggerFactory) -> new KryoSerde(PieState.class.getClassLoader());
            }
        },
        ;

        public abstract Function<LoggerFactory, Serde> get();
    }

    public enum StoreKind {
        in_memory {
            @Override public PieBuilder.StoreFactory get(HierarchicalResource temporaryDirectory) {
                return (serde, resourceService, loggerFactory) -> new InMemoryStore();
            }
        },
        lmdb {
            @Override public PieBuilder.StoreFactory get(HierarchicalResource temporaryDirectory) {
                return (serde, resourceService, loggerFactory) -> new LMDBStore(serde, Objects.requireNonNull(resourceService.toLocalFile(temporaryDirectory.appendRelativePath("lmdb"))), loggerFactory);
            }
        },
        ;

        public abstract PieBuilder.StoreFactory get(HierarchicalResource temporaryDirectory);
    }

    public enum ShareKind {
        non_sharing {
            @Override public Function<LoggerFactory, Share> get() {
                return (logger) -> new NonSharingShare();
            }
        },
        ;

        public abstract Function<LoggerFactory, Share> get();
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
        },
        ;

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
        },
        modified {
            @Override public ResourceStamper<ReadableResource> getReadable() {
                return ResourceStampers.modifiedFile();
            }

            @Override public ResourceStamper<HierarchicalResource> getHierarchical() {
                return ResourceStampers.modifiedFile();
            }
        },
        hash {
            @Override public ResourceStamper<ReadableResource> getReadable() {
                return ResourceStampers.hashFile();
            }

            @Override public ResourceStamper<HierarchicalResource> getHierarchical() {
                return ResourceStampers.hashDirRec();
            }
        },
        ;

        public abstract ResourceStamper<ReadableResource> getReadable();

        public abstract ResourceStamper<HierarchicalResource> getHierarchical();
    }

    public enum LayerKind {
        validation {
            @Override public LayerFactory get() {
                return (taskDefs, loggerFactory, serde) -> new ValidationLayer(taskDefs, loggerFactory, serde);
            }
        },
        validation_pedantic {
            @Override public LayerFactory get() {
                return (taskDefs, logger, serde) -> new ValidationLayer(ValidationLayer.ValidationOptions.all(), taskDefs, logger, serde);
            }
        },
        validation_pedantic_except_serialization {
            @Override public LayerFactory get() {
                return (taskDefs, logger, serde) -> new ValidationLayer(ValidationLayer.ValidationOptions.all_except_serialization(), taskDefs, logger, serde);
            }
        },
        noop {
            @Override public LayerFactory get() {
                return (taskDefs, logger, serde) -> new NoopLayer();
            }
        },
        ;

        public abstract LayerFactory get();
    }

    public enum TracerKind {
        metrics {
            @Override public Function<LoggerFactory, Tracer> get() {
                return (loggerFactory) -> metricsTracer;
            }

            @Override public MetricsTracer getMetricsTracer() { return metricsTracer; }
        },
        logging {
            @Override public Function<LoggerFactory, Tracer> get() {
                return LoggingTracer::new;
            }

            @Override public @Nullable MetricsTracer getMetricsTracer() { return null; }
        },
        metrics_and_logging {
            @Override public Function<LoggerFactory, Tracer> get() {
                return (loggerFactory) -> new CompositeTracer(metricsTracer, new LoggingTracer(loggerFactory));
            }

            @Override public MetricsTracer getMetricsTracer() { return metricsTracer; }
        },
        noop {
            @Override public Function<LoggerFactory, Tracer> get() {
                return (logger) -> NoopTracer.instance;
            }

            @Override public @Nullable MetricsTracer getMetricsTracer() { return null; }
        },
        ;

        private static final MetricsTracer metricsTracer = new MetricsTracer();

        public abstract Function<LoggerFactory, Tracer> get();

        public abstract @Nullable MetricsTracer getMetricsTracer();
    }
}
