package mb.pie.bench.spoofax3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.log.api.Logger;
import mb.pie.api.Task;
import mb.pie.bench.state.ChangesState;
import mb.pie.bench.state.LoggerState;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.Spoofax3CompilerState;
import mb.pie.bench.state.TemporaryDirectoryState;
import mb.pie.bench.util.GarbageCollection;
import mb.resource.hierarchical.HierarchicalResource;
import mb.spoofax.compiler.spoofax3.language.CompilerException;
import mb.spoofax.core.platform.LoggerComponent;
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

    protected LoggerState loggerState;
    protected LoggerComponent loggerComponent;
    protected Logger logger;
    protected TemporaryDirectoryState temporaryDirectoryState;
    protected Spoofax3CompilerState spoofax3CompilerState;
    protected PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(LoggerState loggerState, TemporaryDirectoryState temporaryDirectoryState, Spoofax3CompilerState spoofax3CompilerState, PieState pieState) throws IOException {
        this.loggerState = loggerState;
        this.loggerComponent = loggerState.setupTrial();
        logger = loggerComponent.getLoggerFactory().create(Spoofax3Bench.class);
        logger.trace("Spoofax3Bench.setupTrial");
        this.temporaryDirectoryState = temporaryDirectoryState;
        final HierarchicalResource temporaryDirectory = temporaryDirectoryState.setupTrial();
        this.spoofax3CompilerState = spoofax3CompilerState.setupTrial(loggerComponent);
        this.pieState = pieState.setupTrial(loggerComponent, temporaryDirectory, spoofax3CompilerState.getPie());
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() throws IOException {
        pieState.tearDownTrial();
        pieState = null;
        spoofax3CompilerState.tearDownTrial();
        spoofax3CompilerState = null;
        temporaryDirectoryState.tearDownTrial();
        temporaryDirectoryState = null;
        loggerComponent = null;
        logger.trace("Spoofax3Bench.tearDownTrial");
        logger = null;
    }


    // Invocation


    protected ChangesState changesState;
    protected Task<Result<KeyedMessages, CompilerException>> task;

    @Setup(Level.Invocation)
    public void setupInvocation(ChangesState changesState) throws Exception {
        logger.trace("Spoofax3Bench.setupInvocation");
        final HierarchicalResource temporaryDirectory = temporaryDirectoryState.setupInvocation();
        this.changesState = changesState.setupInvocation(loggerComponent, temporaryDirectory);
        this.task = spoofax3CompilerState.setupInvocation(temporaryDirectory);
        this.pieState.setupInvocation();
    }

    @TearDown(Level.Invocation)
    public void tearDownInvocation() throws Exception {
        this.task = null;
        this.changesState.tearDownInvocation();
        this.changesState = null;

        this.temporaryDirectoryState.tearDownInvocation();
        this.pieState.tearDownInvocation();
        this.spoofax3CompilerState.tearDownInvocation();
        logger.trace("Spoofax3Bench.tearDownInvocation");
    }


    // Benchmarks

    @Benchmark
    public void full(Blackhole blackhole) throws Exception {
        blackhole.consume(pieState.requireTopDownInNewSession(task, "0_initial"));
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changesState.reset(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changesState); // Apply change.
            reset(); // Reset to ensure full build.
            gc(); // Run garbage collection to make memory usage deterministic.
            blackhole.consume(pieState.requireTopDownInNewSession(task, i + "_" + id)); // Run build and measure.
            ++i;
        }
    }

    @Benchmark
    public void incrementalTopDown(Blackhole blackhole) throws Exception {
        blackhole.consume(pieState.requireTopDownInNewSession(task, "0_initial"));
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changesState.reset(); // Reset change maker to clear changed resources.
            final String id = change.applyChange(changesState); // Apply change.
            gc(); // Run garbage collection to make memory usage deterministic.
            blackhole.consume(pieState.requireTopDownInNewSession(task, i + "_" + id)); // Run build and measure.
            ++i;
        }
    }

    @Benchmark
    public void incrementalBottomUp(Blackhole blackhole) throws Exception {
        blackhole.consume(pieState.requireTopDownInNewSession(task, "0_initial"));
        int i = 1;
        for(Spoofax3CompilerState.Change change : spoofax3CompilerState.getChanges()) {
            changesState.reset(); // Reset change maker to clear changed resources.
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
