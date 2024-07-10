package mb.pie.api;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface SerializableConsumer<T extends Serializable> extends Consumer<T>, Serializable {}
