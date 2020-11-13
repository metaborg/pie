package mb.pie.store.lmdb;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Function;

/**
 * Deserialized data, or a failure to deserialize, with mappers to deal with failure and non-existance.
 */
class De<T> {
    final @Nullable T deserialized;
    final boolean failed;

    private De(@Nullable T deserialized, boolean failed) {
        this.deserialized = deserialized;
        this.failed = failed;
    }

    De(@Nullable T deserialized) {
        this(deserialized, false);
    }

    De() {
        this(null, true);
    }

    static <R> R orElse(@Nullable De<R> deserialized, R def) {
        if(deserialized == null || deserialized.failed) {
            return def;
        } else {
            return deserialized.deserialized;
        }
    }

    static <R> R orElseNull(@Nullable De<R> deserialized) {
        if(deserialized == null || deserialized.failed) {
            return null;
        } else {
            return deserialized.deserialized;
        }
    }

    static <R, RR> RR mapOrElse(@Nullable De<R> deserialized, RR def, Function<R, RR> func) {
        if(deserialized == null || deserialized.failed) {
            return def;
        } else {
            return func.apply(deserialized.deserialized);
        }
    }

    static <R, RR> @Nullable RR mapOrElseNull(@Nullable De<R> deserialized, Function<R, RR> func) {
        if(deserialized == null || deserialized.failed) {
            return null;
        } else {
            return func.apply(deserialized.deserialized);
        }
    }
}
