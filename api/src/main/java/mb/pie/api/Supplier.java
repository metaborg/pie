package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;

/**
 * An incremental version of {@link java.util.function.Supplier}: a supplier of a {@link Serializable serializable}
 * object of some type {@code T}, which may be gotten incrementally through an {@link ExecContext execution context},
 * for example by requiring the output of a task or resource. The supplier itself is also {@link Serializable
 * serializable} so that it can be passed as input and outputs of tasks.
 *
 * @param <T> Type of object to supply.
 */
public interface Supplier<T extends @Nullable Serializable> extends Serializable {
    T get(ExecContext context) throws ExecException, IOException, InterruptedException;

    /**
     * Creates a new supplier for which given {@link Function incremental function} is executed on the result of this
     * supplier. {@code T} may not be {@code @Nullable}, because incremental functions may not take {@code null} as
     * input.
     *
     * @param incrFunction Incremental function to apply to result of this provider.
     * @param <R>          Resulting type.
     * @return Transformed supplier.
     */
    default <R extends Serializable> Supplier<R> map(Function<? super T, ? extends R> incrFunction) {
        return new MappedSupplier<>(this, incrFunction);
    }

    /**
     * Creates a new supplier for which given {@link java.util.function.Function function} is executed on the result of
     * this supplier. {@code T} may not be {@code @Nullable}, because incremental functions may not take {@code null} as
     * input. Given function must be {@link Serializable}.
     *
     * @param function Function to apply to result of this provider. Must be {@link Serializable}.
     * @param <R>      Resulting type.
     * @return Transformed supplier.
     */
    default <R extends Serializable> Supplier<R> map(java.util.function.Function<? super T, ? extends R> function) {
        return new MappedSupplier<>(this, new NonIncrFunction<>(function));
    }
}
