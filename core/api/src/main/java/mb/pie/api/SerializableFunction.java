package mb.pie.api;

import java.io.Serializable;

@FunctionalInterface
public interface SerializableFunction<T, R extends Serializable> extends java.util.function.Function<T, R>, Serializable {}
