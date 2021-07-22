package mb.pie.runtime;

import mb.pie.api.ExecException;
import mb.pie.api.MixedSession;
import mb.pie.api.Store;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.TopDownSession;
import mb.pie.api.Tracer;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;
import mb.pie.runtime.exec.BottomUpRunner;
import mb.pie.runtime.exec.TopDownRunner;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;

import java.io.Serializable;
import java.util.HashSet;
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
        Store store,
        Tracer tracer,
        HashSet<ResourceKey> providedResources
    ) {
        super(taskDefs, resourceService, store, tracer, providedResources);
        this.topDownRunner = topDownRunner;
        this.bottomUpRunner = bottomUpRunner;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
    }

    @Override public void close() {
        store.sync();
    }

    @Override
    public TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources, Set<?> tags, CancelToken cancel) throws ExecException, InterruptedException {
        checkUpdateAffectedBy();
        if(changedResources.isEmpty())
            return createSessionAfterBottomUp();
        handleException(() -> bottomUpRunner.requireInitial(changedResources, tags, cancel));
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
        return new TopDownSessionImpl(bottomUpRunner, taskDefs, resourceService, store, tracer, providedResources);
    }


    @Override
    public <O extends Serializable> O require(Task<O> task) throws ExecException, InterruptedException {
        return require(task, NullCancelableToken.instance);
    }

    @SuppressWarnings("ConstantConditions") @Override
    public <O extends Serializable> O require(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException {
        checkRequire("require");
        return handleException(() -> topDownRunner.requireInitial(task, true, cancel));
    }

    @Override
    public <O extends Serializable> O requireWithoutObserving(Task<O> task) throws ExecException, InterruptedException {
        return requireWithoutObserving(task, NullCancelableToken.instance);
    }

    @SuppressWarnings("ConstantConditions") @Override
    public <O extends Serializable> O requireWithoutObserving(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException {
        checkRequire("requireWithoutObserving");
        return handleException(() -> topDownRunner.requireInitial(task, false, cancel));
    }


    private void checkRequire(String name) {
        if(updateAffectedByExecuted) {
            throw new IllegalStateException("Cannot call '" + name + "', because 'updateAffectedBy' has already been called. Use the object returned by 'updateAffectedBy' to get task outputs or to execute new tasks");
        }
        requireExecuted = true;
    }
}
