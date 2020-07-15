package mb.pie.runtime.exec;

import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * [Execution reason][ExecReason] for when a task is (directly or indirectly) affected by a change.
 */
public class AffectedExecReason implements ExecReason {
    @Override public boolean equals(@Nullable Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "directly or indirectly affected by change";
    }
}
