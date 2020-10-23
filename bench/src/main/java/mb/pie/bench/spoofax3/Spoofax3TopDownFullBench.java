package mb.pie.bench.spoofax3;

import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Task;
import mb.pie.bench.state.PieState;
import mb.pie.bench.state.Spoofax3CompilerState;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
// Development/debugging settings
@Warmup(iterations = 1)
@Measurement(iterations = 1)
@Fork(value = 0)
public class Spoofax3TopDownFullBench {
    private Spoofax3CompilerState spoofax3CompilerState;
    private PieState pieState;

    @Setup(Level.Trial)
    public void setupTrial(Spoofax3CompilerState spoofax3CompilerState, PieState pieState) {
        this.spoofax3CompilerState = spoofax3CompilerState.setupTrial();
        this.spoofax3CompilerState.inputBuilder.withParser();
        this.spoofax3CompilerState.inputBuilder.withStyler();
        this.spoofax3CompilerState.inputBuilder.withConstraintAnalyzer();
        this.spoofax3CompilerState.inputBuilder.withStrategoRuntime();

        this.pieState = pieState.setupTrial(spoofax3CompilerState.getPie());
    }


    private Task<Result<KeyedMessages, CompilerException>> task;

    @Setup(Level.Invocation)
    public void setupInvocation() {
        this.task = spoofax3CompilerState.setupInvocation();
    }


    @Benchmark
    public Object exec() throws ExecException, InterruptedException {
        try(final MixedSession session = pieState.newSession()) {
            //noinspection ConstantConditions
            final Result<KeyedMessages, CompilerException> result = session.require(task);
            result.ifErr((e) -> {
                System.out.println(e.getMessage() + ". " + e.getSubMessage());
                e.getSubMessages().ifPresent((System.out::println));
                if(e.getSubCause() != null) {
                    e.getSubCause().printStackTrace(System.out);
                }
            });
            return result;
        }
    }


    @TearDown(Level.Invocation)
    public void tearDownInvocation() throws IOException {
        pieState.tearDownInvocation();
        spoofax3CompilerState.tearDownInvocation();
    }
}
