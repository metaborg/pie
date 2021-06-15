package mb.pie.api;

import com.sun.nio.sctp.IllegalReceiveException;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.HierarchicalResource;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A session is a temporary context in which PIE builds can be executed. Within a session, a task with the same {@link
 * Task#key() key} is never executed more than once. For sound incrementality, a new session must be started after
 * external changes have occurred. External changes include:
 * <ul>
 * <li>Change to the contents or metadata of a required or provided resource (e.g., file contents)</li>
 * <li>Change to the input of a required task, which does not influence its {@link Task#key() key}</li>
 * <li>
 * Change to the {@link OutTransient#getValue() value} or {@link OutTransient#isConsistent() consistency} values of an
 * {@link OutTransient} output of a task
 * </li>
 * <li>
 * Change to the {@link OutTransientEquatable#getEquatableValue() equatable value} of an {@link OutTransientEquatable}
 * output of a task
 * </li>
 * </ul>
 */
public interface Session {
    /**
     * Makes {@code task} up-to-date in a top-down fashion, returning its up-to-date output. Also marks the task as
     * {@link Observability#ExplicitObserved explicitly observed}, indicating that it (and its transitive dependencies)
     * should be kept up-to-date in bottom-up builds.
     *
     * @param task Task to make up-to-date.
     * @return Up-to-date output of {@code task}. May be {@code null} when task returns {@code null}.
     * @throws ExecException        When a task throws an {@link Exception}.
     * @throws InterruptedException When execution is cancelled.
     * @throws RuntimeException     When a task throws a {@link RuntimeException}.
     */
    <O extends Serializable> O require(Task<O> task) throws ExecException, InterruptedException;

    /**
     * Makes {@code task} up-to-date in a top-down fashion, using given {@code cancel} checker, returning its up-to-date
     * output. Also marks the task as {@link Observability#ExplicitObserved explicitly observed}, indicating that it
     * (and its transitive dependencies) should be kept up-to-date in bottom-up builds.
     *
     * @param task   Task to make up-to-date.
     * @param cancel Cancel checker to use.
     * @return Up-to-date output of {@code task}. May be {@code null} when task returns {@code null}.
     * @throws ExecException        When a task throws an {@link Exception}.
     * @throws InterruptedException When execution is cancelled.
     * @throws RuntimeException     When a task throws a {@link RuntimeException}.
     */
    <O extends Serializable> O require(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException;

    /**
     * Makes {@code task} up-to-date in a top-down fashion, returning its up-to-date output, without marking it as
     * {@link Observability#ExplicitObserved explicitly observed}.
     *
     * @param task Task to make up-to-date.
     * @return Up-to-date output of {@code task}. May be {@code null} when task returns {@code null}.
     * @throws ExecException        When a task throws an {@link Exception}.
     * @throws InterruptedException When execution is cancelled.
     * @throws RuntimeException     When a task throws a {@link RuntimeException}.
     */
    <O extends Serializable> O requireWithoutObserving(Task<O> task) throws ExecException, InterruptedException;

    /**
     * Makes {@code task} up-to-date in a top-down fashion, using given {@code cancel} checker, returning its up-to-date
     * output, without marking it as {@link Observability#ExplicitObserved explicitly observed}.
     *
     * @param task   Task to make up-to-date.
     * @param cancel Cancel checker to use.
     * @return Up-to-date output of {@code task}. May be {@code null} when task returns {@code null}.
     * @throws ExecException        When a task throws an {@link Exception}.
     * @throws InterruptedException When execution is cancelled.
     * @throws RuntimeException     When a task throws a {@link RuntimeException}.
     */
    <O extends Serializable> O requireWithoutObserving(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException;


    /**
     * Checks whether {@code task} has been executed at least once.
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task was executed at least once, false otherwise.
     */
    default boolean hasBeenExecuted(Task<?> task) {
        return hasBeenExecuted(task.key());
    }

    /**
     * Checks whether task with given {@code key} has been executed at least once.
     *
     * @param key Key of task to check.
     * @return True if task was executed at least once, false otherwise.
     */
    boolean hasBeenExecuted(TaskKey key);


    /**
     * Checks whether {@code task} is explicitly observed (by requiring it with a top-down build) or implicitly observed
     * (when another observed task requires it).
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task is observed, false otherwise.
     */
    default boolean isObserved(Task<?> task) {
        return isObserved(task.key());
    }

    /**
     * Checks whether task with given {@code key} is explicitly observed (by requiring it with a top-down build) or
     * implicitly observed (when another observed task requires it).
     *
     * @param key Key of task to check.
     * @return True if task is observed, false otherwise.
     */
    boolean isObserved(TaskKey key);


    /**
     * Checks whether {@code task} is explicitly observed (by requiring it with a top-down build).
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task is explicitly observed, false otherwise.
     */
    default boolean isExplicitlyObserved(Task<?> task) {
        return isExplicitlyObserved(task.key());
    }

    /**
     * Checks whether task with given {@code key} is explicitly observed (by requiring it with a top-down build).
     *
     * @param key Key of task to check.
     * @return True if task is explicitly observed, false otherwise.
     */
    boolean isExplicitlyObserved(TaskKey key);


    /**
     * Sets the observability of {@code task} to {@link Observability#ExplicitObserved explicitly observed} if it is
     * {@link Observability#ImplicitObserved implicitly observed}. Does nothing if already {@link
     * Observability#ExplicitObserved explicitly observed}. Throws if {@link Observability#Unobserved unobserved}. Use
     * {@link #require} to explicitly observe an unobserved task.
     *
     * @param task Task to explicitly observe.
     * @throws IllegalReceiveException when {@code} task is not observed.
     */
    default void setImplicitToExplicitlyObserved(Task<?> task) {
        setImplicitToExplicitlyObserved(task.key());
    }

    /**
     * Sets the observability of task with given {@code key} to {@link Observability#ExplicitObserved explicitly
     * observed} if it is {@link Observability#ImplicitObserved implicitly observed}. Does nothing if already {@link
     * Observability#ExplicitObserved explicitly observed}. Throws if {@link Observability#Unobserved unobserved}. Use
     * {@link #require} to explicitly observe an unobserved task.
     *
     * @param key Key of task to explicitly observe.
     * @throws IllegalReceiveException when {@code} task is not observed.
     */
    void setImplicitToExplicitlyObserved(TaskKey key);


    /**
     * Explicitly unobserves {@code task}, settings its observability status to {@link Observability#ImplicitObserved
     * implicitly observed} if it was {@link Observability#ExplicitObserved explicitly observed} but still observed by
     * another observed task. Otherwise, sets the observability status to {@link Observability#Unobserved unobserved}
     * and then propagates this to required tasks. Unobserved tasks are not considered in bottom-up builds, and can be
     * garbage collected with {@link #deleteUnobservedTasks}.
     *
     * @param task Task to unobserve.
     */
    void unobserve(Task<?> task);

    /**
     * Explicitly unobserves task for {@code key}, settings its observability status to {@link
     * Observability#ImplicitObserved implicitly observed} if it was {@link Observability#ExplicitObserved explicitly
     * observed} but still observed by another observed task. Otherwise, sets the observability status to {@link
     * Observability#Unobserved unobserved} and then propagates this to required tasks. Unobserved tasks are not
     * considered in bottom-up builds, and can be garbage collected with {@link #deleteUnobservedTasks}.
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
    void deleteUnobservedTasks(Predicate<Task<?>> shouldDeleteTask, BiPredicate<Task<?>, HierarchicalResource> shouldDeleteProvidedResource) throws IOException;


    /**
     * Gets the resources that were provided by executed tasks so far this session.
     *
     * @return Read-only set of provided resources.
     */
    Set<ResourceKey> getProvidedResources();
}
