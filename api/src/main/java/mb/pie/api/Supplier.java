package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

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
     * Creates a new supplier for which given {@code function} is executed on the result of this supplier. The given
     * function must be {@link Serializable serializable}, otherwise the returned supplier cannot be serialized.
     *
     * @param function Function to apply to result of this provider.
     * @param <R>      Resulting type.
     * @return Transformed provider.
     */
    default <R extends @Nullable Serializable> Supplier<R> map(Function<? super T, ? extends R> function) {
        return new MappedSupplier<>(this, function);
    }
}
