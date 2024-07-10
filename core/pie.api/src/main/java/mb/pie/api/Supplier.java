package mb.pie.api;

import java.io.Serializable;

/**
 * An incremental version of {@link java.util.function.Supplier}: a supplier of a {@link Serializable serializable}
 * object of some type {@code T}, which may be gotten incrementally through an {@link ExecContext execution context},
 * for example by requiring the output of a task or resource.
 *
 * The supplier itself is {@link Serializable serializable} and has identity so that it can be passed as input and
 * outputs of tasks.
 *
 * @param <T> Type of object to supply.
 * @implNote Must implement {@link Object#equals(Object)} and {@link Object#hashCode()} as this supplier can be passed
 * as inputs to tasks, or produced as output from tasks, which require these implementations for incrementality.
 */
@FunctionalInterface
public interface Supplier<T extends Serializable> extends Serializable {
    /**
     * Incrementally gets the value.
     *
     * @param context Execution context for incrementality.
     * @return Output object. May be {@code null}.
     */
    T get(ExecContext context);

    /**
     * Creates a new supplier for which given {@link Function incremental function} is executed on the result of this
     * supplier.
     *
     * @param mapper {@link Function Incremental function} to apply to result of this supplier.
     * @param <R>    Resulting type.
     * @return Mapped supplier.
     */
    default <R extends Serializable> Supplier<R> map(Function<? super T, ? extends R> mapper) {
        return new MappedSupplier<>(this, mapper);
    }

    /**
     * Creates a new supplier for which given {@link SerializableFunction serializable function} is executed on the
     * result of this supplier.
     *
     * @param mapper {@link SerializableFunction Serializable function} to apply to result of this supplier.
     * @param <R>    Resulting type.
     * @return Mapped supplier.
     */
    default <R extends Serializable> Supplier<R> map(SerializableFunction<? super T, ? extends R> mapper) {
        return new MappedSupplier<>(this, new NonIncrFunction<>(mapper));
    }


    /**
     * Creates a new supplier for which given {@link Function incremental function} is executed on the result of this
     * supplier, which returns a new supplier.
     *
     * @param mapper {@link Function Incremental function} to apply to result of this supplier.
     * @param <R>    Resulting type.
     * @return Mapped supplier.
     */
    default <R extends Serializable> Supplier<R> flatMap(Function<? super T, Supplier<R>> mapper) {
        return new FlatMappedSupplier<>(this, mapper);
    }

    /**
     * Creates a new supplier for which given {@link SerializableFunction serializable function} is executed on the
     * result of this supplier, which returns a new supplier.
     *
     * @param mapper {@link SerializableFunction Serializable function} to apply to result of this supplier.
     * @param <R>    Resulting type.
     * @return Mapped supplier.
     */
    default <R extends Serializable> Supplier<R> flatMap(SerializableFunction<? super T, Supplier<R>> mapper) {
        return new FlatMappedSupplier<>(this, new NonIncrFunction<>(mapper));
    }
}
