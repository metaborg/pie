package mb.pie.bench.util;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.runtime.exec.Stats;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.AggregationPolicy;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.ResultRole;
import org.openjdk.jmh.results.ScalarResult;
import org.openjdk.jmh.results.SingleShotResult;
import org.openjdk.jmh.runner.IterationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class PieMetricsProfiler implements InternalProfiler {
    private static @Nullable PieMetricsProfiler instance;

    private Logger logger;

    public PieMetricsProfiler() {
        PieMetricsProfiler.instance = this;
    }

    public static PieMetricsProfiler getInstance(LoggerFactory loggerFactory) {
        if(instance == null) {
            throw new IllegalStateException("PIE metrics profiler has not been initialized yet");
        }
        instance.logger = loggerFactory.create(PieMetricsProfiler.class);
        return instance;
    }


    private final Timer timer = new Timer();
    private final ArrayList<Measurement> measurements = new ArrayList<>();
    private boolean measurementsActive = false;


    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        if(iterationParams.getType() != IterationType.MEASUREMENT) return; // Do not measure warmup.
        timer.reset();
        measurements.clear();
        Stats.reset();
        measurementsActive = true;
    }

    public void start(String id) {
        if(!measurementsActive) return;
        timer.start();
        Stats.reset();
        logger.info("Start measuring: " + id);
    }

    public void stop(String id) {
        if(!measurementsActive) return;
        final Timer.Time time = timer.stop();
        measurements.add(new Measurement(id, time, Stats.requires, Stats.executions, Stats.fileReqs, Stats.fileGens, Stats.callReqs));
        logger.info("Done measuring: " + id);
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        final ArrayList<Result> results = new ArrayList<>();
        if(!measurementsActive) return results;
        final TimeUnit targetTimeUnit = benchmarkParams.getTimeUnit();
        for(Measurement measurement : measurements) {
            results.add(measurement.createSingleShotResult("systemNanoTime", measurement.time.systemNanoTime, targetTimeUnit));
            results.add(measurement.createSingleShotResult("threadCpuTime", measurement.time.threadCpuTime, targetTimeUnit));
            results.add(measurement.createSingleShotResult("threadUserTime", measurement.time.threadUserTime, targetTimeUnit));
            results.add(measurement.createAvgScalarResult("requiredTasks", measurement.requiredTasks, "tasks"));
            results.add(measurement.createAvgScalarResult("executedTasks", measurement.executedTasks, "tasks"));
            results.add(measurement.createAvgScalarResult("requiredResourceDependencies", measurement.requiredResourceDependencies, "dependencies"));
            results.add(measurement.createAvgScalarResult("providedResourceDependencies", measurement.providedResourceDependencies, "dependencies"));
            results.add(measurement.createAvgScalarResult("requiredTaskDependencies", measurement.requiredTaskDependencies, "dependencies"));
        }
        measurementsActive = false;
        return results;
    }

    @Override
    public String getDescription() {
        return "PIE metrics profiler";
    }


    private static class Measurement {
        public final String id;
        public final Timer.Time time;
        public final long requiredTasks;
        public final long executedTasks;
        public final long requiredResourceDependencies;
        public final long providedResourceDependencies;
        public final long requiredTaskDependencies;

        private Measurement(
            String id,
            Timer.Time time,
            long requiredTasks,
            long executedTasks,
            long requiredResourceDependencies,
            long providedResourceDependencies,
            long requiredTaskDependencies
        ) {
            this.id = id;
            this.time = time;
            this.requiredTasks = requiredTasks;
            this.executedTasks = executedTasks;
            this.requiredResourceDependencies = requiredResourceDependencies;
            this.providedResourceDependencies = providedResourceDependencies;
            this.requiredTaskDependencies = requiredTaskDependencies;
        }

        public SingleShotResult createSingleShotResult(String name, long duration, TimeUnit outputTimeUnit) {
            return new SingleShotResult(ResultRole.SECONDARY, id + ":" + name, duration, outputTimeUnit);
        }

        public ScalarResult createScalarResult(String name, double n, String unit, AggregationPolicy policy) {
            return new ScalarResult(id + ":" + name, n, unit, policy);
        }

        public ScalarResult createAvgScalarResult(String name, double n, String unit) {
            return createScalarResult(name, n, unit, AggregationPolicy.AVG);
        }
    }
}
