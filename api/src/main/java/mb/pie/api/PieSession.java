package mb.pie.api;

import mb.pie.api.exec.Cancelled;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A session for incrementally executing PIE tasks, supporting two different traversal strategies:
 * <ul>
 * <li>
 * Top-down (scanning): given a root task to execute, check whether the task must be executed by checking its required
 * and provided resources, and by recursively checking required tasks, resulting in a top-down depth-first traversal of
 * the dependency graph. This can also mark the task as an {@link Observability#ExplicitObserved explicitly observed}
 * task, indicating that it, and its transitive dependencies, should be kept up-to-date in bottom-up builds.
 * </li>
 * <li>
 * Bottom-up (change-driven): given a set of changed resources, schedule all directly affected tasks in a topologically
 * sorted dependency queue, pop and execute tasks from the front of the queue until it is empty, where changed outputs
 * or provided resources of an executed task cause dependent affected tasks to be scheduled into the queue. Only tasks
 * that are {@link Observability#ExplicitObserved explicitly observed} or {@link Observability#ImplicitObserved
 * implicitly observed} are considered.
 * </li>
 * </ul>
 * <p>
 * When executing a new task, or changing the input to a task, top-down execution must be used, as bottom-up execution
 * can only detect changes which originate from resources. When changes to resources are known, prefer bottom-up
 * execution, as it only traverses the affected part of the dependency graph, which is more efficient than top-down
 * execution which traverses the entire dependency graph rooted at a given task.
 * <p>
 * Within a session, a task with the same {@link Task#key() key} is never executed more than once. For sound
 * incrementality, a new session must be started after external changes have occurred. External changes include:
 * <ul>
 * <li>Change to the contents or metadata of a required or provided resource (e.g., file contents)</li>
 * <li>Change to the input of a required task, which does not influence its {@link Task#key() key}</li>
 * <li>Change to the output of a task, which does not correspond to a change in its input</li>
 * <li>
 * Change to the {@link OutTransient#getValue() value} or {@link OutTransient#isConsistent() consistency} values of an
 * {@link OutTransient} output of a task
 * </li>
 * <li>
 * Change to the {@link OutTransientEquatable#getEquatableValue() equatable value} of an {@link OutTransientEquatable}
 * output of a task
 * </li>
 * </ul>
 * <p>
 * Outputs of required tasks are observed by the observers {@link Pie#setCallback set} in the {@link Pie} object this
 * session was created from.
 * <p>
 * When using a {@link Cancelled cancel checker}, execution is cancelled between task executions by throwing an {@link
 * InterruptedException}.
 */
public interface PieSession extends AutoCloseable {
    /**
     * Makes {@code task} up-to-date in a top-down fashion, returning its up-to-date output. Also marks the task as
     * {@link Observability#ExplicitObserved explicitly observed}, indicating that it (and its transitive dependencies)
     * should be kept up-to-date in bottom-up builds.
     *
     * @param task Task to make up-to-date.
     * @return Up-to-date output of {@code task}.
     * @throws ExecException When an executing task throws an exception.
     */
    <O extends Serializable> O require(Task<O> task) throws ExecException;

    /**
     * Makes {@code task} up-to-date in a top-down fashion, using given {@code cancel} checker, returning its up-to-date
     * output. Also marks the task as {@link Observability#ExplicitObserved explicitly observed}, indicating that it
     * (and its transitive dependencies) should be kept up-to-date in bottom-up builds.
     *
     * @param task   Task to make up-to-date.
     * @param cancel Cancel checker to use.
     * @return Up-to-date output of {@code task}.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O require(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException;


    /**
     * Make up-to-date all tasks (transitively) affected by {@code changedResources} in a bottom-up fashion. Only {@link
     * Observability#ExplicitObserved explicitly observed} or {@link Observability#ImplicitObserved implicitly observed}
     * tasks are considered.
     *
     * @param changedResources Set of {@link ResourceKey resource key}s which have been changed.
     * @throws ExecException When an executing task throws an exception.
     */
    void updateAffectedBy(Set<? extends ResourceKey> changedResources) throws ExecException;

    /**
     * Make up-to-date all tasks (transitively) affected by {@code changedResources} in a bottom-up fashion, using given
     * {@code cancel} checker. Only {@link Observability#ExplicitObserved explicitly observed} or {@link
     * Observability#ImplicitObserved implicitly observed} tasks are considered.
     *
     * @param changedResources Set of {@link ResourceKey resource key}s which have been changed.
     * @param cancel           Cancel checker to use.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    void updateAffectedBy(Set<? extends ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException;


    /**
     * Makes {@code task} up-to-date in a top-down fashion, returning its up-to-date output, without marking it as
     * {@link Observability#ExplicitObserved explicitly observed}.
     *
     * @param task Task to make up-to-date.
     * @return Up-to-date output of {@code task}.
     * @throws ExecException When an executing task throws an exception.
     */
    <O extends Serializable> O requireWithoutObserving(Task<O> task) throws ExecException;

    /**
     * Makes {@code task} up-to-date in a top-down fashion, using given {@code cancel} checker, returning its up-to-date
     * output, without marking it as {@link Observability#ExplicitObserved explicitly observed}.
     *
     * @param task   Task to make up-to-date.
     * @param cancel Cancel checker to use.
     * @return Up-to-date output of {@code task}.
     * @throws ExecException        When an executing task throws an exception.
     * @throws InterruptedException When execution is cancelled.
     */
    <O extends @Nullable Serializable> O requireWithoutObserving(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException;


    /**
     * Explicitly unobserves {@code task}, settings its observability status to {@link Observability#ImplicitObserved
     * implicitly observed} if it was {@link Observability#ExplicitObserved explicitly observed} but still observed by
     * another observed task. Otherwise, sets the observability status to {@link Observability#Unobserved unobserved}
     * and then propagates this to required tasks. Unobserved tasks are not considered in bottom-up builds.
     *
     * @param task Task to unobserve.
     */
    void unobserve(Task<?> task);

    /**
     * Explicitly unobserves task for {@code key}, settings its observability status to {@link
     * Observability#ImplicitObserved implicitly observed} if it was {@link Observability#ExplicitObserved explicitly
     * observed} but still observed by another observed task. Otherwise, sets the observability status to {@link
     * Observability#Unobserved unobserved} and then propagates this to required tasks. Unobserved tasks are not
     * considered in bottom-up builds.
     *
     * @param key Key of task to unobserve.
     */
    void unobserve(TaskKey key);


    /**
     * Deletes {@link Observability#Unobserved unobserved} tasks from the store, and deletes provided resources of
     * deleted tasks.
     *
     * @param shouldDeleteTask             Function that gets called to determine if a task should be deleted. When this
     *                                     function returns false, the unobserved task and its unobserved task
     *                                     requirements will not be deleted.
     * @param shouldDeleteProvidedResource Function that gets called to determine if a provided resource of a deleted
     *                                     task should be deleted. When this function returns false, the provided file
     *                                     will not be deleted.
     * @throws IOException when deleting a resource fails unexpectedly.
     */
    void deleteUnobservedTasks(Function<Task<?>, Boolean> shouldDeleteTask, BiFunction<Task<?>, Resource, Boolean> shouldDeleteProvidedResource) throws IOException;


    @Override void close();
}
