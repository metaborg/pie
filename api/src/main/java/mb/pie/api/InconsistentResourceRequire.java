package mb.pie.api;

import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.ResourceStamp;

/**
 * Execution reason for inconsistent resource requires dependency.
 */
public class InconsistentResourceRequire implements ExecReason {
    public final ResourceRequireDep dep;
    public final ResourceStamp<?> newStamp;


    public InconsistentResourceRequire(ResourceRequireDep dep, ResourceStamp<?> newStamp) {
        this.dep = dep;
        this.newStamp = newStamp;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final InconsistentResourceRequire that = (InconsistentResourceRequire)o;
        if(!dep.equals(that.dep)) return false;
        return newStamp.equals(that.newStamp);
    }

    @Override public int hashCode() {
        int result = dep.hashCode();
        result = 31 * result + newStamp.hashCode();
        return result;
    }

    @Override public String toString() {
        return "inconsistent required resource " + dep.key;
    }
}
