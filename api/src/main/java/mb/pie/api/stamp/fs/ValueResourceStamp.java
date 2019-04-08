package mb.pie.api.stamp.fs;

import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class ValueResourceStamp<R extends Resource> implements ResourceStamp<R> {
    private final @Nullable Serializable value;
    private final ResourceStamper<R> stamper;

    public ValueResourceStamp(@Nullable Serializable value, ResourceStamper<R> stamper) {
        this.value = value;
        this.stamper = stamper;
    }

    @Override public ResourceStamper<R> getStamper() {
        return stamper;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ValueResourceStamp<?> that = (ValueResourceStamp<?>) o;
        if(!Objects.equals(value, that.value)) return false;
        return stamper.equals(that.stamper);
    }

    @Override public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + stamper.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ValueResourceStamp(" + value + ", " + stamper + ')';
    }
}
