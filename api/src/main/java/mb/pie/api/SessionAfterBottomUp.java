package mb.pie.api;

import mb.pie.api.exec.CancelToken;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Second stage of {@link PieSession}, after running a bottom-up build with resource changes. This second stage is used
 * to get outputs of existing tasks or to execute new tasks.
 */
public interface SessionAfterBottomUp extends SessionBase {
    /**
     * Gets the up-to-date output of an existing {@code task}. This method throws an exception when used on tasks that
     * have not been executed before (i.e, a new task). For new tasks, use {@link #require} or {@link
     * #requireWithoutObserving} instead.
     *
     * @param task Task to get result for.
     * @return Up-to-date output of {@code task}.
     * @throws IllegalStateException When {@code task} has not been executed before or when its input object differs
     *                               from an existing task with the same {@link TaskKey key}.
     */
    <O extends @Nullable Serializable> O getOutput(Task<O> task);


    /**
     * Makes {@code task} up-to-date in a top-down fashion, returning its up-to-date output. Also marks the task as
     * {@link Observability#ExplicitObserved explicitly observed}, indicating that it (and its transitive dependencies)
     * should be kept up-to-date in bottom-up builds.
     *
     * @param task Task to make up-to-date.
     * @return Up-to-date output of {@code task}.
     * @throws ExecException When an executing task throws an exception.
     */
    <O extends @Nullable Serializable> O require(Task<O> task) throws ExecException;

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
    <O extends @Nullable Serializable> O require(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException;

    /**
     * Makes {@code task} up-to-date in a top-down fashion, returning its up-to-date output, without marking it as
     * {@link Observability#ExplicitObserved explicitly observed}.
     *
     * @param task Task to make up-to-date.
     * @return Up-to-date output of {@code task}.
     * @throws ExecException When an executing task throws an exception.
     */
    <O extends @Nullable Serializable> O requireWithoutObserving(Task<O> task) throws ExecException;

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
    <O extends @Nullable Serializable> O requireWithoutObserving(Task<O> task, CancelToken cancel) throws ExecException, InterruptedException;
}
