package mb.pie.bench.spoofax3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
// Development/debugging settings
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 0)
public class Spoofax3Bench {
    // Trial

    protected TemporaryDirectoryState temporaryDirectoryState;
    protected Spoofax3CompilerState spoofax3CompilerState;
    protected ChangeMaker changeMaker;
    protected PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(TemporaryDirectoryState temporaryDirectoryState, Spoofax3CompilerState spoofax3CompilerState, PieState pieState) throws Exception {
        this.temporaryDirectoryState = temporaryDirectoryState.setupTrial();
        this.spoofax3CompilerState = spoofax3CompilerState.setupTrial(this.temporaryDirectoryState.getTemporaryDirectory());
        this.changeMaker = spoofax3CompilerState.getChangeMaker();
        this.pieState = pieState.setupTrial(spoofax3CompilerState.getPie());
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() throws Exception {
        this.spoofax3CompilerState.tearDownTrial();
        this.temporaryDirectoryState.tearDown();
    }


    // Invocation

    protected Task<Result<KeyedMessages, CompilerException>> task;

    @Setup(Level.Invocation)
    public void setupInvocation() throws Exception {
        this.task = spoofax3CompilerState.setupInvocation();
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() throws Exception {
        reset();
    }


    // Benchmarks

    @Benchmark
    public void topDownFull(Blackhole blackhole) throws Exception {
        pieState.requireTopDownInNewSession(task, "0_initial");
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changeMaker.reset(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changeMaker); // Apply change.
            reset(); // Reset to ensure full build.
            gc(); // Run garbage collection to make memory usage deterministic.
            pieState.requireTopDownInNewSession(task, i + "_" + id); // Run build and measure.
            ++i;
        }
    }

    @Benchmark
    public void topDownIncremental(Blackhole blackhole) throws Exception {
        pieState.requireTopDownInNewSession(task, "0_initial");
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changeMaker.reset(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changeMaker); // Apply change.
            gc(); // Run garbage collection to make memory usage deterministic.
            pieState.requireTopDownInNewSession(task, i + "_" + id); // Run build and measure.
            ++i;
        }
    }

    @Benchmark
    public void bottomUpIncremental(Blackhole blackhole) throws Exception {
        pieState.requireTopDownInNewSession(task, "0_initial");
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changeMaker.reset(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changeMaker); // Apply change.
            gc(); // Run garbage collection to make memory usage deterministic.
            pieState.requireBottomUpInNewSession(changeMaker.getChangedResources(), i + "_" + id); // Run build and measure.
            ++i;
        }
    }


    // Helper methods

    private void reset() throws IOException {
        // Delete generated files and PIE storage to reset back to initial state.
        spoofax3CompilerState.deleteGeneratedFiles();
        pieState.dropStore();
    }

    private void gc() {
        GarbageCollection.run();
    }
}
