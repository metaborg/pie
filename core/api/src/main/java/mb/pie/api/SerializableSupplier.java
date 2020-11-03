package mb.pie.api;

import java.io.Serializable;
import java.util.function.Supplier;

@FunctionalInterface
public interface SerializableSupplier<T extends Serializable> extends Supplier<T>, Serializable {}
