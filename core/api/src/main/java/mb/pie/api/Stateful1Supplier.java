package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public abstract class Stateful1Supplier<S extends Serializable, T extends Serializable> implements Supplier<T> {
    protected final S state;

    public Stateful1Supplier(S state) {
        this.state = state;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Stateful1Supplier<?, ?> that = (Stateful1Supplier<?, ?>)o;
        return state.equals(that.state);
    }

    @Override public int hashCode() {
        return state.hashCode();
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "(" + state + ")";
    }
}
