package mb.pie.runtime.exec;

import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * [Execution reason][ExecReason] for when a task is (directly or indirectly) affected by a change.
 */
public class AffectedExecReason implements ExecReason {
    @Override
    public boolean equals(@Nullable Object other) {
        if(this == other) return true;
        if(other != null && other.getClass() != getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "directly or indirectly affected by change";
    }
}
