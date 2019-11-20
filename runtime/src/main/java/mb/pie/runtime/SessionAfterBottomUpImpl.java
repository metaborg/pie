package mb.pie.runtime;

import mb.pie.api.ExecException;
import mb.pie.api.Output;
import mb.pie.api.SessionAfterBottomUp;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.runtime.exec.TopDownSession;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class SessionAfterBottomUpImpl extends SessionBaseImpl implements SessionAfterBottomUp {
    private final TopDownSession topDownSession;
    private final Store store;


    public SessionAfterBottomUpImpl(TopDownSession topDownSession, TaskDefs taskDefs, ResourceService resourceService, Store store) {
        super(taskDefs, resourceService, store);
        this.topDownSession = topDownSession;
        this.store = store;
    }


    @Override public <O extends @Nullable Serializable> O getOutput(Task<O> task) {
        try(final StoreReadTxn txn = store.readTxn()) {
            final TaskKey key = task.key();
            final @Nullable Serializable input = txn.input(key);
            if(input == null) {
                throw new IllegalStateException("Cannot get output of task '" + task + "', it does not exist. Call require to execute this new task");
            }
            if(!input.equals(task.input)) {
                throw new IllegalStateException("Cannot get output of task '" + task + "', its stored input '" + input + "' differs from given input '" + task.input + "'. Create a new session to execute this task");
            }
            final @Nullable Output output = txn.output(key);
            if(output == null) {
                throw new IllegalStateException("Cannot get output of task '" + task + "', it has no output object. Call require to execute this new task");
            }
            @SuppressWarnings({"ConstantConditions", "unchecked"}) final O out = (O)output.output;
            //noinspection ConstantConditions
            return out;
        }
    }


    @Override
    public <O extends Serializable> O require(Task<O> task) throws ExecException {
        try {
            return require(task, NullCancelableToken.instance);
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException {
        return topDownSession.requireInitial(task, true, cancel);
    }

    @Override
    public <O extends Serializable> O requireWithoutObserving(Task<O> task) throws ExecException {
        try {
            return requireWithoutObserving(task, NullCancelableToken.instance);
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public <O extends @Nullable Serializable> O requireWithoutObserving(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException {
        return topDownSession.requireInitial(task, false, cancel);
    }
}
