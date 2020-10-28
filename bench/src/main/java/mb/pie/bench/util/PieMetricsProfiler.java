package mb.pie.bench.util;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class PieMetricsProfiler implements InternalProfiler {
    private static @Nullable PieMetricsProfiler instance;

    public PieMetricsProfiler() {
        PieMetricsProfiler.instance = this;
    }

    public static PieMetricsProfiler getInstance() {
        if(instance == null) {
            throw new IllegalStateException("PIE metrics profiler has not been initialized yet");
        }
        return instance;
    }


    private final Timer timer = new Timer();
    private final ArrayList<Measurement> measurements = new ArrayList<>();


    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        timer.reset();
        measurements.clear();
        Stats.reset();
    }

    public void start() {
        timer.start();
        Stats.reset();
    }

    public void stop(String id) {
        final Timer.Time time = timer.stop();
        measurements.add(new Measurement(id, time, Stats.requires, Stats.executions, Stats.fileReqs, Stats.fileGens, Stats.callReqs));
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        final TimeUnit targetTimeUnit = benchmarkParams.getTimeUnit();
        final ArrayList<Result> results = new ArrayList<>();
        for(Measurement measurement : measurements) {
            results.add(measurement.createSingleShotResult("systemNanoTime", measurement.time.systemNanoTime, targetTimeUnit));
            results.add(measurement.createSingleShotResult("threadCpuTime", measurement.time.threadCpuTime, targetTimeUnit));
            results.add(measurement.createSingleShotResult("threadUserTime", measurement.time.threadUserTime, targetTimeUnit));
            results.add(measurement.createMaxScalarResult("requiredTasks", measurement.requiredTasks, "tasks"));
            results.add(measurement.createMaxScalarResult("executedTasks", measurement.executedTasks, "tasks"));
            results.add(measurement.createMaxScalarResult("requiredResourceDependencies", measurement.requiredResourceDependencies, "dependencies"));
            results.add(measurement.createMaxScalarResult("providedResourceDependencies", measurement.providedResourceDependencies, "dependencies"));
            results.add(measurement.createMaxScalarResult("requiredTaskDependencies", measurement.requiredTaskDependencies, "dependencies"));
        }
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

        public ScalarResult createMaxScalarResult(String name, double n, String unit) {
            return createScalarResult(name, n, unit, AggregationPolicy.MAX);
        }
    }
}
