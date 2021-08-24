package mb.pie.api;

import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.NullCancelableToken;

import java.io.Serializable;

/**
 * Second stage of {@link MixedSession}, after running a bottom-up build with resource changes. This second stage is
 * used to get outputs of existing tasks or to execute new tasks using top-down builds.
 */
public interface TopDownSession extends Session {
    /**
     * Gets the up-to-date output of an existing {@code task}. This method throws an exception when used on tasks that
     * have not been executed before (i.e, a new task or different input). For new tasks, use {@link #require} or {@link
     * #requireWithoutObserving} instead.
     *
     * @param task Task to get result for.
     * @return Up-to-date output of {@code task}. May be {@code null} if the task returns {@code null}.
     * @throws IllegalStateException When {@code task} has not been executed before or when its input object differs
     *                               from an existing task with the same {@link TaskKey key}.
     */
    <O extends Serializable> O getOutput(Task<O> task);


    /**
     * Gets the up-to-date output of {@code task} if it has been executed before and is observed, and ensures that it is
     * explicitly observed. If not, the task is required.
     *
     * @param task Task to get output for.
     * @return Up-to-date output of {@code task}. May be {@code null} if the task returns {@code null}.
     * @throws ExecException         When {@link #require} throws.
     * @throws InterruptedException  When {@link #require} throws.
     * @throws IllegalStateException When {@link #getOutput} throws.
     */
    default <O extends Serializable> O getOutputOrRequireAndEnsureExplicitlyObserved(
        Task<O> task
    ) throws ExecException, InterruptedException {
        return getOutputOrRequireAndEnsureExplicitlyObserved(task, NullCancelableToken.instance);
    }

    /**
     * Gets the up-to-date output of {@code task} if it has been executed before and is observed, and ensures that it is
     * explicitly observed. If not, the task is required.
     *
     * @param task   Task to get output for.
     * @param cancel Cancel checker to pass to {@link #require} if needed.
     * @return Up-to-date output of {@code task}. May be {@code null} if the task returns {@code null}.
     * @throws ExecException         When {@link #require} throws.
     * @throws InterruptedException  When {@link #require} throws.
     * @throws IllegalStateException When {@link #getOutput} throws.
     */
    default <O extends Serializable> O getOutputOrRequireAndEnsureExplicitlyObserved(
        Task<O> task,
        CancelToken cancel
    ) throws ExecException, InterruptedException {
        if(!hasBeenExecuted(task) || !isObserved(task)) {
            return require(task, cancel);
        } else {
            if(!isExplicitlyObserved(task)) {
                setImplicitToExplicitlyObserved(task);
            }
            final O output = getOutput(task);
            if(output instanceof OutTransient<?>) {
                final OutTransient<?> outTransient = (OutTransient<?>)output;
                if(!outTransient.isConsistent()) {
                    return require(task, cancel);
                }
            }
            return output;
        }
    }
}
