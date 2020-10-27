package mb.pie.bench.spoofax3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.Spoofax3CompilerState;
import mb.pie.bench.state.TemporaryDirectoryState;
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
// Development/debugging settings
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 0)
public class Spoofax3TopDownFullBench {
    private TemporaryDirectoryState temporaryDirectoryState;
    private Spoofax3CompilerState spoofax3CompilerState;
    private PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(TemporaryDirectoryState temporaryDirectoryState, Spoofax3CompilerState spoofax3CompilerState, PieState pieState) throws Exception {
        this.temporaryDirectoryState = temporaryDirectoryState.setupTrial();
        this.spoofax3CompilerState = spoofax3CompilerState.setupTrial(this.temporaryDirectoryState.getTemporaryDirectory());
        this.pieState = pieState.setupTrial(spoofax3CompilerState.getPie());
    }


    private Task<Result<KeyedMessages, CompilerException>> task;

    @Setup(Level.Invocation)
    public void setupInvocation() throws Exception {
        this.task = spoofax3CompilerState.setupInvocation();
    }

    @Benchmark
    public Object run() throws Exception {
        try(final MixedSession session = pieState.newSession()) {
            return spoofax3CompilerState.require(session, task);
        }
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() throws Exception {
        pieState.tearDownInvocation();
        spoofax3CompilerState.deleteGeneratedFiles();
    }


    @TearDown(Level.Trial)
    public void tearDownTrial() throws Exception {
        this.spoofax3CompilerState.tearDownTrial();
        this.temporaryDirectoryState.tearDown();
    }
}
