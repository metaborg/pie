package mb.pie.api;

import mb.pie.api.stamp.OutputStamp;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Task 'require' (calls) dependency.
 */
public class TaskRequireDep implements Serializable {
    public final TaskKey callee;
    public final OutputStamp stamp;


    public TaskRequireDep(TaskKey callee, OutputStamp stamp) {
        this.callee = callee;
        this.stamp = stamp;
    }


    /**
     * @return an execution reason when this call requirement is inconsistent w.r.t. [output], `null` otherwise.
     */
    public @Nullable InconsistentTaskRequire checkConsistency(@Nullable Serializable output) {
        final OutputStamp newStamp = stamp.getStamper().stamp(output);
        if(!stamp.equals(newStamp)) {
            return new InconsistentTaskRequire(this, newStamp);
        }
        return null;
    }

    /**
     * @return `true` when this call requirement is consistent w.r.t. [output], `false` otherwise.
     */
    public Boolean isConsistent(@Nullable Serializable output) {
        final OutputStamp newStamp = stamp.getStamper().stamp(output);
        return stamp.equals(newStamp);
    }

    /**
     * @return `true` when this call requirement's callee is equal to [other], `false` otherwise.
     */
    public Boolean calleeEqual(TaskKey other) {
        return other.equals(callee);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TaskRequireDep that = (TaskRequireDep)o;
        if(!callee.equals(that.callee)) return false;
        return stamp.equals(that.stamp);
    }

    @Override public int hashCode() {
        int result = callee.hashCode();
        result = 31 * result + stamp.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TaskReq(" + callee.toShortString(100) + ", " + stamp + ")";
    }
}
