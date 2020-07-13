package mb.pie.runtime.exec;

import mb.pie.api.exec.ExecReason;

import java.io.Serializable;
import java.util.Objects;

public class InconsistentInput implements ExecReason {
    public final Serializable oldInput;
    public final Serializable newInput;

    public InconsistentInput(Serializable oldInput, Serializable newInput) {
        this.oldInput = oldInput;
        this.newInput = newInput;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final InconsistentInput that = (InconsistentInput) o;
        return oldInput.equals(that.oldInput) && newInput.equals(that.newInput);
    }

    @Override public int hashCode() {
        return Objects.hash(oldInput, newInput);
    }

    @Override public String toString() {
        return "inconsistent input";
    }
}
