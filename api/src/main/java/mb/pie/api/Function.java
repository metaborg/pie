package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;

/**
 * An incremental version of {@link java.util.function.Function}: a function from a {@link Serializable serializable}
 * object of some type {@code T} to a {@link Serializable serializable} object of some type {@code R}, which may be
 * produced incrementally through an {@link ExecContext execution context}, for example by executing a task. The
 * function itself is also {@link Serializable serializable} so that it can be passed as input and outputs of tasks.
 *
 * @param <T> Type of the input to the function.
 * @param <R> Type of the output of the function.
 */
public interface Function<T extends Serializable, R extends @Nullable Serializable> extends Serializable {
    R apply(ExecContext context, T input) throws ExecException, InterruptedException;

    /**
     * Creates a new function for which given {@link Function incremental function} is executed on the input before
     * applying this function.
     *
     * @param before Incremental function to apply to the input before applying of this incremental function.
     * @param <B>    New type of input.
     * @return Transformed function.
     */
    default <B extends @Nullable Serializable> Function<B, R> mapInput(Function<? super B, ? extends T> before) {
        return new MappedFunctionInput<>(this, before);
    }

    /**
     * Creates a new function for which given {@link Function incremental function} is executed on the output of
     * applying this function.
     *
     * @param after Incremental function to apply to result of this incremental function.
     * @param <A>   New type of output.
     * @return Transformed function.
     */
    default <A extends @Nullable Serializable> Function<T, A> mapOutput(Function<? super R, ? extends A> after) {
        return new MappedFunctionOutput<>(this, after);
    }

    /**
     * Creates a new function for which given {@link java.util.function.Function function} is executed on the input
     * before applying this function. Given function must be {@link Serializable}.
     *
     * @param before Function to apply to the input before applying of this incremental function. Must be {@link
     *               Serializable}
     * @param <B>    New type of input.
     * @return Transformed function.
     */
    default <B extends @Nullable Serializable> Function<B, R> mapInput(java.util.function.Function<? super B, ? extends T> before) {
        return new MappedFunctionInput<>(this, new NonIncrFunction<>(before));
    }

    /**
     * Creates a new function for which given {@link java.util.function.Function function} is executed on the output of
     * applying this function. Given function must be {@link Serializable}.
     *
     * @param after Function to apply to result of this incremental function. Must be {@link Serializable}
     * @param <A>   New type of output.
     * @return Transformed function.
     */
    default <A extends @Nullable Serializable> Function<T, A> mapOutput(java.util.function.Function<? super R, ? extends A> after) {
        return new MappedFunctionOutput<>(this, new NonIncrFunction<>(after));
    }
}
