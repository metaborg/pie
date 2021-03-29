package mb.pie.api;

import java.io.Serializable;

/**
 * An incremental version of {@link java.util.function.Function}: a function from a {@link Serializable serializable}
 * object of some type {@link T} to a {@link Serializable serializable} object of some type {@link R}, which may be
 * produced incrementally through an {@link ExecContext execution context}, for example by executing a task.
 *
 * The function itself is {@link Serializable serializable} and has identity so that it can be passed as input and
 * outputs of tasks.
 *
 * @param <T> Type of the input to the function.
 * @param <R> Type of the output of the function.
 * @implNote Must implement {@link Object#equals(Object)} and {@link Object#hashCode()} as this function can be passed
 * as inputs to tasks, or produced as output from tasks, which require these implementations for incrementality.
 */
public interface Function<T extends Serializable, R extends Serializable> extends Serializable {
    /**
     * Incrementally applies the function.
     *
     * @param context Execution context for incrementality.
     * @param input   Input object. May be {@code null} when mapped from another function that returns {@code null}.
     * @return Output object. May be {@code null}.
     */
    R apply(ExecContext context, T input);

    /**
     * Creates a new function for which given {@link Function incremental function} is executed on the input before
     * applying this function.
     *
     * @param before {@link Function Incremental function} to apply to the input before applying of this incremental
     *               function.
     * @param <B>    New type of input.
     * @return Transformed function.
     */
    default <B extends Serializable> Function<B, R> mapInput(Function<? super B, ? extends T> before) {
        return new MappedFunctionInput<>(this, before);
    }

    /**
     * Creates a new function for which given {@link Function incremental function} is executed on the output of
     * applying this function.
     *
     * @param after {@link Function Incremental function} to apply to result of this incremental function.
     * @param <A>   New type of output.
     * @return Transformed function.
     */
    default <A extends Serializable> Function<T, A> mapOutput(Function<? super R, ? extends A> after) {
        return new MappedFunctionOutput<>(this, after);
    }

    /**
     * Creates a new function for which given {@link SerializableFunction serializable function} is executed on the
     * input before applying this function.
     *
     * @param before {@link SerializableFunction Serializable function} to apply to the input before applying of this
     *               incremental function.
     * @param <B>    New type of input.
     * @return Transformed function.
     */
    default <B extends Serializable> Function<B, R> mapInput(SerializableFunction<? super B, ? extends T> before) {
        return new MappedFunctionInput<>(this, new NonIncrFunction<>(before));
    }

    /**
     * Creates a new function for which given {@link SerializableFunction serializable function} is executed on the
     * output of applying this function.
     *
     * @param after {@link SerializableFunction Serializable function} to apply to result of this incremental function.
     * @param <A>   New type of output.
     * @return Transformed function.
     */
    default <A extends Serializable> Function<T, A> mapOutput(SerializableFunction<? super R, ? extends A> after) {
        return new MappedFunctionOutput<>(this, new NonIncrFunction<>(after));
    }

    /**
     * Creates an {@link Supplier incremental supplier} for this function with given {@code input}. An incremental
     * supplier is {@link Serializable} and as such can be used as an input or output of a task.
     *
     * @param input The input for on top of which this function will be executed.
     * @return {@link Supplier} based on a function.
     */
    default Supplier<R> createSupplier(T input) {
        return new FunctionBasedSupplier<>(this, input);
    }
}
