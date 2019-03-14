package mb.pie.store.lmdb;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Function;

public class Deserialized<R extends @Nullable Serializable> {
    public final R deserialized;
    public final boolean failed;

    public Deserialized(R deserialized, boolean failed) {
        this.deserialized = deserialized;
        this.failed = failed;
    }

    public Deserialized(R deserialized) {
        this(deserialized, false);
    }

    public Deserialized() {
        this(null, true);
    }

    public static <R extends @Nullable Serializable> R orElse(@Nullable Deserialized<R> deserialized, R def) {
        if(deserialized == null || deserialized.failed) {
            return def;
        } else {
            return deserialized.deserialized;
        }
    }

    public static <R extends @Nullable Serializable, RR> RR mapOrElse(@Nullable Deserialized<R> deserialized, RR def, Function<R, RR> func) {
        if(deserialized == null || deserialized.failed) {
            return def;
        } else {
            return func.apply(deserialized.deserialized);
        }
    }
}
