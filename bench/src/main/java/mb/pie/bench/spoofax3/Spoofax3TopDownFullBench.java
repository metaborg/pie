package mb.pie.bench.spoofax3;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.Spoofax3CompilerState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class Spoofax3TopDownFullBench {
    private Spoofax3CompilerState spoofax3CompilerState;
    private PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(Spoofax3CompilerState spoofax3CompilerState, PieState pieState) {
        this.spoofax3CompilerState = spoofax3CompilerState.setup();
        this.pieState = pieState.setup(spoofax3CompilerState.getPie());
    }

    @Setup(Level.Invocation)
    public void setupInvocation() {
        pieState.reset();
    }

    @Benchmark
    public Object exec() throws ExecException, InterruptedException {
        try(final MixedSession session = pieState.newSession()) {
            //noinspection ConstantConditions
            return session.require(spoofax3CompilerState.buildTask());
        }
    }
}
