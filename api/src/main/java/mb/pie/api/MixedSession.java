package mb.pie.api;

import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;

import java.util.Set;

/**
 * A session for incrementally executing PIE tasks, supporting two different traversal strategies:
 * <ul>
 * <li>
 * Bottom-up (change-driven): given a set of changed resources, schedule all directly affected tasks in a topologically
 * sorted dependency queue, pop and execute tasks from the front of the queue until it is empty, where changed outputs
 * or provided resources of an executed task cause dependent affected tasks to be scheduled into the queue. Only tasks
 * that are {@link Observability#ExplicitObserved explicitly observed} or {@link Observability#ImplicitObserved
 * implicitly observed} are considered.
 * </li>
 * <li>
 * Top-down (scanning): given a root task to execute, check whether the task must be executed by checking its required
 * and provided resources, and by recursively checking required tasks, resulting in a top-down depth-first traversal of
 * the dependency graph. This can also mark the task as an {@link Observability#ExplicitObserved explicitly observed}
 * task, indicating that it, and its transitive dependencies, should be kept up-to-date in bottom-up builds.
 * </li>
 * </ul>
 * <p>
 * When executing a new task, or changing the input to a task, top-down execution must be used, as bottom-up execution
 * can only detect changes which originate from resources. When changes to resources are known, prefer bottom-up
 * execution, as it only traverses the affected part of the dependency graph, which is more efficient than top-down
 * execution which traverses the entire dependency graph rooted at a given task.
 * <p>
 * When mixing a bottom-up build with top-down builds, a single {@link #updateAffectedBy bottom-up build} must be
 * executed first, which then returns a new session object of type {@link TopDownSession} which can be used to get task outputs or to execute new
 * tasks.
 * <p>
 * Outputs of required tasks are observed by the observers {@link Pie#setCallback set} in the {@link Pie} object this
 * session was created from.
 * <p>
 * When using a {@link CancelToken cancel checker}, execution is cancelled between task executions by throwing an {@link
 * InterruptedException}.
 *
 * @see Session for information on when a new session should be started.
 */
public interface MixedSession extends Session, AutoCloseable {
    /**
     * Make up-to-date all tasks (transitively) affected by {@code changedResources} in a bottom-up fashion. Only {@link
     * Observability#ExplicitObserved explicitly observed} or {@link Observability#ImplicitObserved implicitly observed}
     * tasks are considered.
     *
     * @param changedResources Set of {@link ResourceKey resource key}s which have been changed.
     * @throws ExecException         When a task throws an {@link Exception}.
     * @throws InterruptedException  When execution is cancelled.
     * @throws RuntimeException      When a task throws a {@link RuntimeException}.
     * @throws IllegalStateException When executed after a call to {@link #updateAffectedBy} or {@link #require}. Only
     *                               one {@link #updateAffectedBy bottom-up build} may be executed per session. Use the
     *                               returned object to query task results or to execute new tasks.
     */
    TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources) throws ExecException, InterruptedException;

    /**
     * Make up-to-date all tasks (transitively) affected by {@code changedResources} in a bottom-up fashion, using given
     * {@code cancel} checker. Only {@link Observability#ExplicitObserved explicitly observed} or {@link
     * Observability#ImplicitObserved implicitly observed} tasks are considered.
     *
     * @param changedResources Set of {@link ResourceKey resource key}s which have been changed.
     * @param cancel           Cancel checker to use.
     * @throws ExecException         When a task throws an {@link Exception}.
     * @throws InterruptedException  When execution is cancelled.
     * @throws RuntimeException      When a task throws a {@link RuntimeException}.
     * @throws IllegalStateException When executed after a call to {@link #updateAffectedBy} or {@link #require}. Only
     *                               one {@link #updateAffectedBy bottom-up build} may be executed per session. Use the
     *                               returned object to query task results or to execute new tasks.
     */
    TopDownSession updateAffectedBy(Set<? extends ResourceKey> changedResources, CancelToken cancel) throws ExecException, InterruptedException;


    /**
     * Closes the session, {@link Store#sync() synchronizing the storage to persistent storage, if any}.
     *
     * @throws RuntimeException when closing fails.
     */
    @Override void close();
}
