package mb.pie.bench.util;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.runtime.tracer.MetricsTracer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
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

    private @MonotonicNonNull Logger logger;
    private @Nullable MetricsTracer metricsTracer;

    public PieMetricsProfiler() {
        PieMetricsProfiler.instance = this;
    }

    public static PieMetricsProfiler getInstance(LoggerFactory loggerFactory, MetricsTracer metricsTracer) {
        if(instance == null) {
            throw new IllegalStateException("PIE metrics profiler has not been initialized yet");
        }
        instance.logger = loggerFactory.create(PieMetricsProfiler.class);
        instance.metricsTracer = metricsTracer;
        return instance;
    }


    private final Timer timer = new Timer();
    private final ArrayList<Measurement> measurements = new ArrayList<>();
    private boolean measurementsActive = false;


    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        if(iterationParams.getType() != IterationType.MEASUREMENT) return; // Do not measure warmup.
        measurements.clear();
        measurementsActive = true;
    }

    public void start(String id) {
        if(!measurementsActive) return;
        timer.start();
        if(metricsTracer != null) {
            metricsTracer.reset();
        }
        logger.info("Start measuring: " + id);
    }

    public void stop(String id) {
        if(!measurementsActive) return;
        final Timer.Time time = timer.stop();
        final MetricsTracer.@Nullable Report report;
        if(metricsTracer != null) {
            report = metricsTracer.reportAndReset();
        } else {
            report = null;
        }
        measurements.add(new Measurement(id, time, report));
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

            if(measurement.report != null) {
                results.add(measurement.createAvgScalarResult("providedResources", measurement.report.totalProvidedResources, "resources"));
                results.add(measurement.createAvgScalarResult("requiredResources", measurement.report.totalRequiredResources, "resources"));
                results.add(measurement.createAvgScalarResult("requiredTasks", measurement.report.totalRequiredTasks, "tasks"));

                results.add(measurement.createAvgScalarResult("checkedProvidedResourceDependencies", measurement.report.totalCheckedProvidedResourceDependencyCount, "dependencies"));
                results.add(measurement.createAvgScalarResult("checkedRequiredResourceDependencies", measurement.report.totalCheckedRequiredResourceDependencyCount, "dependencies"));
                results.add(measurement.createAvgScalarResult("checkedRequiredTaskDependencies", measurement.report.totalCheckedRequiredTaskDependencyCount, "dependencies"));

                results.add(measurement.createAvgScalarResult("executedTasks", measurement.report.totalExecutionCount, "tasks"));
            }
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
        public final MetricsTracer.@Nullable Report report;

        private Measurement(
            String id,
            Timer.Time time,
            MetricsTracer.@Nullable Report report
        ) {
            this.id = id;
            this.time = time;
            this.report = report;
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
