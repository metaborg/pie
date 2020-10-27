package mb.pie.bench.spoofax3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.Spoofax3CompilerState;
import mb.pie.bench.state.TemporaryDirectoryState;
import mb.pie.bench.util.ChangeMaker;
import mb.pie.bench.util.GarbageCollection;
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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
// Development/debugging settings
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@Fork(value = 0)
public class Spoofax3TopDownIncrBench {
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
    public void run(Blackhole blackhole) throws Exception {
        // Initial
        try(final MixedSession session = pieState.newSession()) {
            blackhole.consume(spoofax3CompilerState.require(session, task, "initial"));
        }
        // Changes
        int i = 0;
        final ChangeMaker changeMaker = spoofax3CompilerState.getChangeMaker();
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            // Apply change
            final String id = change.applyChange(changeMaker);
            // Run garbage collection to make memory usage deterministic.
            GarbageCollection.run();
            // Measure incremental
            try(final MixedSession session = pieState.newSession()) {
                blackhole.consume(spoofax3CompilerState.require(session, task, "incr:" + i + ":" + id));
            }
            // Delete generated files and PIE storage
            spoofax3CompilerState.deleteGeneratedFiles();
            pieState.dropStore();
            // Run garbage collection to make memory usage deterministic.
            GarbageCollection.run();
            // Measure full
            try(final MixedSession session = pieState.newSession()) {
                blackhole.consume(spoofax3CompilerState.require(session, task, "full:" + i + ":" + id));
            }
            ++i;
        }
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() throws Exception {
        spoofax3CompilerState.deleteGeneratedFiles();
        pieState.dropStore();
    }


    @TearDown(Level.Trial)
    public void tearDownTrial() throws Exception {
        this.spoofax3CompilerState.tearDownTrial();
        this.temporaryDirectoryState.tearDown();
    }
}
