package mb.pie.store.lmdb;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Function;

class Deserialized<R extends @Nullable Serializable> {
    final R deserialized;
    final boolean failed;

    private Deserialized(R deserialized, boolean failed) {
        this.deserialized = deserialized;
        this.failed = failed;
    }

    Deserialized(R deserialized) {
        this(deserialized, false);
    }

    Deserialized() {
        this(null, true);
    }

    static <R extends @Nullable Serializable> R orElse(@Nullable Deserialized<R> deserialized, R def) {
        if(deserialized == null || deserialized.failed) {
            return def;
        } else {
            return deserialized.deserialized;
        }
    }

    static <R extends @Nullable Serializable> R orElseNull(@Nullable Deserialized<R> deserialized) {
        if(deserialized == null || deserialized.failed) {
            return null;
        } else {
            return deserialized.deserialized;
        }
    }

    static <R extends @Nullable Serializable, RR> RR mapOrElse(@Nullable Deserialized<R> deserialized, RR def, Function<R, RR> func) {
        if(deserialized == null || deserialized.failed) {
            return def;
        } else {
            return func.apply(deserialized.deserialized);
        }
    }

    static <R extends @Nullable Serializable, @Nullable RR> RR mapOrElseNull(@Nullable Deserialized<R> deserialized, Function<R, RR> func) {
        if(deserialized == null || deserialized.failed) {
            return null;
        } else {
            return func.apply(deserialized.deserialized);
        }
    }
}
