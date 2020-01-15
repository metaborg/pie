package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

/**
 * A provider of a ({@link Serializable serializable}) object of some type {@code T}, which may be gotten incrementally
 * through an {@link ExecContext execution context}, for example by requiring the output of a task or resource. The
 * provider itself is also {@link Serializable serializable} for incrementality.
 *
 * @param <T> Type of object to provide.
 */
public interface Provider<T extends @Nullable Serializable> extends Serializable {
    T get(ExecContext context) throws ExecException, IOException, InterruptedException;

    /**
     * Creates a new provider for which given {@code function} is executed on the result of this provider. The given
     * function must be {@link Serializable serializable}, otherwise the returned provider cannot be serialized.
     *
     * @param function Function to apply to result of this provider.
     * @param <R>      Resulting type.
     * @return Transformed provider.
     */
    default <R extends @Nullable Serializable> Provider<R> map(Function<T, R> function) {
        return new MappedProvider<>(this, function);
    }
}
