package mb.pie.api;

import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.OutputStamp;

/**
 * Execution reason for inconsistent task requires dependency.
 */
public class InconsistentTaskRequire implements ExecReason {
    public final TaskRequireDep dep;
    public final OutputStamp newStamp;


    public InconsistentTaskRequire(TaskRequireDep dep, OutputStamp newStamp) {
        this.dep = dep;
        this.newStamp = newStamp;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final InconsistentTaskRequire that = (InconsistentTaskRequire)o;
        if(!dep.equals(that.dep)) return false;
        return newStamp.equals(that.newStamp);
    }

    @Override public int hashCode() {
        int result = dep.hashCode();
        result = 31 * result + newStamp.hashCode();
        return result;
    }

    @Override public String toString() {
        return "inconsistent required task " + dep.callee.toShortString(100);
    }
}
