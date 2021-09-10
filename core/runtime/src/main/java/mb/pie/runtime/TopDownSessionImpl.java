package mb.pie.runtime;

import mb.pie.api.ExecException;
import mb.pie.api.Output;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.TopDownSession;
import mb.pie.api.Tracer;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.runtime.exec.BottomUpRunner;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashSet;

public class TopDownSessionImpl extends SessionImpl implements TopDownSession {
    private final BottomUpRunner bottomUpRunner;
    private final Store store;


    public TopDownSessionImpl(
        BottomUpRunner bottomUpRunner,
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Tracer tracer,
        HashSet<ResourceKey> providedResources
    ) {
        super(taskDefs, resourceService, store, tracer, providedResources);
        this.bottomUpRunner = bottomUpRunner;
        this.store = store;
    }


    @SuppressWarnings({"ConstantConditions"}) @Override
    public <O extends Serializable> O getOutput(Task<O> task) {
        try(final StoreReadTxn txn = store.readTxn()) {
            final TaskKey key = task.key();
            final @Nullable Serializable input = txn.getInput(key);
            if(input == null) {
                throw new IllegalStateException("Cannot get output of task '" + task + "', it does not exist. Call require to execute this new task");
            }
            if(!input.equals(task.input)) {
                throw new IllegalStateException("Cannot get output of task '" + task + "', its stored input '" + input + "' differs from given input '" + task.input + "'. Create a new session to execute this task");
            }
            final @Nullable Output output = txn.getOutput(key);
            if(output == null) {
                throw new IllegalStateException("Cannot get output of task '" + task + "', it has no output object. Call require to execute this new task");
            }
            // noinspection unchecked (cast is safe because task must return object of type O)
            return (O)output.output;
        }
    }


    @Override
    public <O extends Serializable> O require(Task<O> task) throws ExecException, InterruptedException {
        return require(task, NullCancelableToken.instance);
    }

    @SuppressWarnings("ConstantConditions") @Override
    public <O extends Serializable> O require(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException {
        return handleException(() -> bottomUpRunner.requireInitial(task, true, cancel));
    }

    @Override
    public <O extends Serializable> O requireWithoutObserving(Task<O> task) throws ExecException, InterruptedException {
        return requireWithoutObserving(task, NullCancelableToken.instance);
    }

    @SuppressWarnings("ConstantConditions") @Override
    public <O extends Serializable> O requireWithoutObserving(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException {
        return handleException(() -> bottomUpRunner.requireInitial(task, false, cancel));
    }
}
