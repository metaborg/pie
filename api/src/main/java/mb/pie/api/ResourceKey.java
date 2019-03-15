package mb.pie.api;

import java.io.Serializable;

/**
 * Unique key of a resource consisting of the [resource system identifier][id] and [key] uniquely identifying the resource within the
 * resource system.
 */
public class ResourceKey implements Serializable {
    public final String id;
    public final Serializable key;


    public ResourceKey(String id, Serializable key) {
        this.id = id;
        this.key = key;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ResourceKey that = (ResourceKey) o;
        if(!id.equals(that.id)) return false;
        return key.equals(that.key);
    }

    @Override public int hashCode() {
        // PERF TODO: cache hashCode, as in the Kotlin implementation?
        int result = id.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    public String toShortString(int maxLength) {
        return "#" + id + ":" + StringUtil.toShortString(key.toString(), maxLength);
    }

    @Override public String toString() {
        return toShortString(100);
    }
}
