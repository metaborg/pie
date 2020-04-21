package mb.pie.runtime;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Store;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.TopDownSession;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.runtime.exec.BottomUpRunner;
import mb.pie.runtime.exec.TopDownRunner;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class MixedSessionImpl extends SessionImpl implements MixedSession {
    protected final TopDownRunner topDownRunner;
    protected final BottomUpRunner bottomUpRunner;

    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Store store;

    private boolean updateAffectedByExecuted = false;
    private boolean requireExecuted = false;


    public MixedSessionImpl(
        TopDownRunner topDownRunner,
        BottomUpRunner bottomUpRunner,
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store
    ) {
        super(taskDefs, resourceService, store);
        this.topDownRunner = topDownRunner;
        this.bottomUpRunner = bottomUpRunner;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
    }

    @Override public void close() throws IOException {
        store.sync();
    }


    @Override
    public TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources) throws ExecException {
        try {
            return updateAffectedBy(changedResources, NullCancelableToken.instance);
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources, CancelToken cancel) throws ExecException, InterruptedException {
        checkUpdateAffectedBy();
        if(changedResources.isEmpty())
            return createSessionAfterBottomUp();
        bottomUpRunner.requireInitial(changedResources, cancel);
        return createSessionAfterBottomUp();
    }

    private void checkUpdateAffectedBy() {
        if(updateAffectedByExecuted) {
            throw new IllegalStateException("Cannot call 'updateAffectedBy' because it has already been called. Create a new session to call 'updateAffectedBy' again");
        }
        if(requireExecuted) {
            throw new IllegalStateException("Cannot call 'updateAffectedBy' because 'require' or 'requireWithoutObserving' has already been called. Create a new session to call 'updateAffectedBy'");
        }
        updateAffectedByExecuted = true;
    }

    private TopDownSession createSessionAfterBottomUp() {
        return new TopDownSessionImpl(topDownRunner, taskDefs, resourceService, store);
    }


    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task) throws ExecException {
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
        return topDownRunner.requireInitial(task, true, cancel);
    }

    @Override
    public <O extends @Nullable Serializable> O requireWithoutObserving(Task<O> task) throws ExecException {
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
        return topDownRunner.requireInitial(task, false, cancel);
    }

    private void checkRequire(String name) {
        if(updateAffectedByExecuted) {
            throw new IllegalStateException("Cannot call '" + name + "', because 'updateAffectedBy' has already been called. Use the object returned by 'updateAffectedBy' to get task outputs or to execute new tasks");
        }
        requireExecuted = true;
    }
}
