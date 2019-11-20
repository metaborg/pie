package mb.pie.runtime;

import mb.pie.api.ExecException;
import mb.pie.api.PieSession;
import mb.pie.api.SessionAfterBottomUp;
import mb.pie.api.Store;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.runtime.exec.BottomUpSession;
import mb.pie.runtime.exec.TopDownSession;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PieSessionImpl extends SessionBaseImpl implements PieSession {
    protected final TopDownSession topDownSession;
    protected final BottomUpSession bottomUpSession;

    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Store store;
    protected final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> callbacks;

    private boolean bottomUpExecuted = false;


    public PieSessionImpl(
        TopDownSession topDownSession,
        BottomUpSession bottomUpSession,
        TaskDefs taskDefs,
        ResourceService resourceService, Store store,
        ConcurrentHashMap<TaskKey, Consumer<Serializable>> callbacks
    ) {
        super(taskDefs, resourceService, store);
        this.topDownSession = topDownSession;
        this.bottomUpSession = bottomUpSession;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.callbacks = callbacks;
    }

    @Override public void close() {
        store.sync();
    }


    @Override
    public SessionAfterBottomUp updateAffectedBy(Set<? extends ResourceKey> changedResources) throws ExecException {
        try {
            return updateAffectedBy(changedResources, NullCancelableToken.instance);
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public SessionAfterBottomUp updateAffectedBy(Set<? extends ResourceKey> changedResources, CancelToken cancel) throws ExecException, InterruptedException {
        checkUpdateAffectedBy();
        if(changedResources.isEmpty())
            return createSessionAfterBottomUp();
        bottomUpSession.requireInitial(changedResources, cancel);
        return createSessionAfterBottomUp();
    }

    private void checkUpdateAffectedBy() {
        if(bottomUpExecuted) {
            throw new IllegalStateException("Cannot call 'updateAffectedBy', because 'updateAffectedBy', 'require', or 'requireWithoutObserving' have already been called. Create a new session to call 'updateAffectedBy' again.");
        }
        bottomUpExecuted = true;
    }

    private SessionAfterBottomUp createSessionAfterBottomUp() {
        return new SessionAfterBottomUpImpl(topDownSession, taskDefs, resourceService, store);
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
        checkRequire("require");
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
        checkRequire("requireWithoutObserving");
        return topDownSession.requireInitial(task, false, cancel);
    }

    private void checkRequire(String name) {
        if(bottomUpExecuted) {
            throw new IllegalStateException("Cannot call '" + name + "', because 'updateAffectedBy' has already been called. Use the object returned by 'updateAffectedBy' to get task outputs or to execute new tasks.");
        }
    }
}
