package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

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
     * Creates a new function for which given {@code before} is executed on the input before applying this function. The
     * given function must be {@link Serializable serializable}, otherwise the returned function cannot be serialized.
     *
     * @param before Function to apply to the input before applying of this function.
     * @param <B>    New type of input.
     * @return Transformed function.
     */
    default <B extends @Nullable Serializable> Function<B, R> mapInput(java.util.function.Function<? super B, ? extends T> before) {
        return new MappedFunctionInput<>(this, before);
    }

    /**
     * Creates a new function for which given {@code after} is executed on the output of applying this function. The
     * given function must be {@link Serializable serializable}, otherwise the returned function cannot be serialized.
     *
     * @param after Function to apply to result of this function.
     * @param <A>   New type of output.
     * @return Transformed function.
     */
    default <A extends @Nullable Serializable> Function<T, A> mapOutput(java.util.function.Function<? super R, ? extends A> after) {
        return new MappedFunctionOutput<>(this, after);
    }
}
