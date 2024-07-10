package mb.pie.runtime.exec;

import mb.pie.api.OutTransient;
import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * [Execution reason][ExecReason] for when the transient output of a task is inconsistent.
 */
public class InconsistentTransientOutput implements ExecReason {
    public final OutTransient<?> inconsistentOutput;

    public InconsistentTransientOutput(OutTransient<?> inconsistentOutput) {
        this.inconsistentOutput = inconsistentOutput;
    }

    /**
     * @return an InconsistentTransientOutput when given output is transient and not consistent, `null` otherwise.
     */
    public static @Nullable InconsistentTransientOutput checkOutput(@Nullable Serializable output) {
        if(output instanceof OutTransient<?>) {
            final OutTransient<?> outTransient = (OutTransient<?>) output;
            if(outTransient.isConsistent()) {
                return null;
            } else {
                return new InconsistentTransientOutput(outTransient);
            }
        } else {
            return null;
        }
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final InconsistentTransientOutput that = (InconsistentTransientOutput) o;
        return inconsistentOutput.equals(that.inconsistentOutput);
    }

    @Override public int hashCode() {
        return Objects.hash(inconsistentOutput);
    }

    @Override public String toString() {
        return "inconsistent transient output";
    }
}
