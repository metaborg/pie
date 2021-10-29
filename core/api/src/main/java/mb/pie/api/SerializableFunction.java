package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

@FunctionalInterface
public interface SerializableFunction<T, R extends @Nullable Serializable> extends java.util.function.Function<T, R>, Serializable {}
