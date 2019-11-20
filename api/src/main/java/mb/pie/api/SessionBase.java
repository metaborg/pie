package mb.pie.api;

import mb.resource.Resource;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

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
public interface SessionBase {
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
    void deleteUnobservedTasks(Function<Task<?>, Boolean> shouldDeleteTask, BiFunction<Task<?>, Resource, Boolean> shouldDeleteProvidedResource) throws IOException;
}
