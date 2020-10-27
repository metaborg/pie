package mb.pie.bench.util;

import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;

import java.util.ArrayList;
import java.util.Collection;

public class PieMetricsProfiler implements InternalProfiler {
    @Override public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {

    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        return new ArrayList<>();
    }

    @Override public String getDescription() {
        return "PIE metrics profiler";
    }
}
