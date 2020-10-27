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
        final long timeNs = timer.stop();
        measurements.add(new Measurement(id, timeNs, Stats.requires, Stats.executions, Stats.fileReqs, Stats.fileGens, Stats.callReqs));
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        final TimeUnit targetTimeUnit = benchmarkParams.getTimeUnit();
        final ArrayList<Result> results = new ArrayList<>();
        for(Measurement measurement : measurements) {
            results.add(measurement.createSingleShotResult("time", measurement.timeNs, targetTimeUnit));
            results.add(measurement.createMaxScalarResult("numTaskRequires", measurement.numTaskRequires, "#"));
            results.add(measurement.createMaxScalarResult("numTaskExecutions", measurement.numTaskExecutions, "#"));
            results.add(measurement.createMaxScalarResult("numResourceDepRequires", measurement.numResourceDepRequires, "#"));
            results.add(measurement.createMaxScalarResult("numResourceDepProvides", measurement.numResourceDepProvides, "#"));
            results.add(measurement.createMaxScalarResult("numTaskDepRequires", measurement.numTaskDepRequires, "#"));
        }
        return results;
    }

    @Override
    public String getDescription() {
        return "PIE metrics profiler";
    }


    private static class Measurement {
        public final String id;
        public final long timeNs;
        public final long numTaskRequires;
        public final long numTaskExecutions;
        public final long numResourceDepRequires;
        public final long numResourceDepProvides;
        public final long numTaskDepRequires;

        private Measurement(
            String id,
            long timeNs,
            long numTaskRequires,
            long numTaskExecutions,
            long numResourceDepRequires,
            long numResourceDepProvides,
            long numTaskDepRequires
        ) {
            this.id = id;
            this.timeNs = timeNs;
            this.numTaskRequires = numTaskRequires;
            this.numTaskExecutions = numTaskExecutions;
            this.numResourceDepRequires = numResourceDepRequires;
            this.numResourceDepProvides = numResourceDepProvides;
            this.numTaskDepRequires = numTaskDepRequires;
        }

        public SingleShotResult createSingleShotResult(String name, long duration, TimeUnit outputTimeUnit) {
            return new SingleShotResult(ResultRole.SECONDARY, name + ":" + id, duration, outputTimeUnit);
        }

        public ScalarResult createScalarResult(String name, double n, String unit, AggregationPolicy policy) {
            return new ScalarResult(name + ":" + id, n, unit, policy);
        }

        public ScalarResult createMaxScalarResult(String name, double n, String unit) {
            return createScalarResult(name, n, unit, AggregationPolicy.MAX);
        }
    }
}
