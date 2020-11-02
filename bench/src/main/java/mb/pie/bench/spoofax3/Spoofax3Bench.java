package mb.pie.bench.spoofax3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.Task;
import mb.pie.bench.state.ChangesState;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.Spoofax3CompilerState;
import mb.pie.bench.state.TemporaryDirectoryState;
import mb.pie.bench.util.GarbageCollection;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class Spoofax3Bench {
    // Trial

    protected @Nullable Spoofax3CompilerState spoofax3CompilerState;
    protected @Nullable PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(Spoofax3CompilerState spoofax3CompilerState, PieState pieState) {
        this.spoofax3CompilerState = spoofax3CompilerState.setupTrial();
        this.pieState = pieState.setupTrial(spoofax3CompilerState.getPie());
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        this.pieState.tearDownTrial();
        this.pieState = null;
        this.spoofax3CompilerState.tearDownTrial();
        this.spoofax3CompilerState = null;
    }


    // Invocation

    protected TemporaryDirectoryState temporaryDirectoryState;
    protected ChangesState changesState;
    protected Task<Result<KeyedMessages, CompilerException>> task;

    @Setup(Level.Invocation)
    public void setupInvocation(TemporaryDirectoryState temporaryDirectoryState, ChangesState changesState) throws Exception {
        this.temporaryDirectoryState = temporaryDirectoryState.setupInvocation();
        final HierarchicalResource temporaryDirectory = temporaryDirectoryState.getTemporaryDirectory();
        this.changesState = changesState.setupInvocation(temporaryDirectory);

        this.task = spoofax3CompilerState.setupInvocation(temporaryDirectory);
        this.pieState.setupInvocation();
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() throws Exception {
        this.task = null;
        this.changesState.tearDownInvocation();
        this.changesState = null;
        this.temporaryDirectoryState.tearDownInvocation();
        this.temporaryDirectoryState = null;

        this.pieState.tearDownInvocation();
        this.spoofax3CompilerState.tearDownInvocation();
    }


    // Benchmarks

    @Benchmark
    public void full(Blackhole blackhole) throws Exception {
        pieState.requireTopDownInNewSession(task, "0_initial");
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changesState.tearDownInvocation(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changesState); // Apply change.
            reset(); // Reset to ensure full build.
            gc(); // Run garbage collection to make memory usage deterministic.
            pieState.requireTopDownInNewSession(task, i + "_" + id); // Run build and measure.
            ++i;
        }
    }

    @Benchmark
    public void incrementalTopDown(Blackhole blackhole) throws Exception {
        pieState.requireTopDownInNewSession(task, "0_initial");
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changesState.tearDownInvocation(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changesState); // Apply change.
            gc(); // Run garbage collection to make memory usage deterministic.
            pieState.requireTopDownInNewSession(task, i + "_" + id); // Run build and measure.
            ++i;
        }
    }

    @Benchmark
    public void incrementalBottomUp(Blackhole blackhole) throws Exception {
        pieState.requireTopDownInNewSession(task, "0_initial");
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changesState.tearDownInvocation(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changesState); // Apply change.
            gc(); // Run garbage collection to make memory usage deterministic.
            pieState.requireBottomUpInNewSession(changesState.getChangedResources(), i + "_" + id); // Run build and measure.
            ++i;
        }
    }


    // Helper methods

    private void reset() throws IOException {
        // Delete generated files and PIE storage to reset back to initial state.
        spoofax3CompilerState.deleteGeneratedFiles();
        pieState.resetState();
    }

    private void gc() {
        GarbageCollection.run();
    }
}
