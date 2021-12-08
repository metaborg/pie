package mb.pie.api;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * A tag indicating whether a bottom-up build is used in an interactive or non-interactive setting, primarily used to
 * defer non-interactive tasks in interactive bottom-up builds with {@link TaskDef#shouldExecWhenAffected}.
 */
public enum Interactivity implements Serializable {
    /**
     * Tag indicating an interactive setting where expensive non-interactive tasks should be deferred. Use in {@link
     * MixedSession#updateAffectedBy} to indicate that a build is interactive.
     */
    Interactive,
    /**
     * Tag indicating a non-interactive setting where expensive non-interactive tasks can be executed normally. Use in
     * {@link MixedSession#updateAffectedBy} to indicate that a build is non-interactive, and use in {@link
     * TaskDef#shouldExecWhenAffected} to indicate that a task is non-interactive.
     */
    NonInteractive;

    public Set<Interactivity> asSingletonSet() {
        return Collections.singleton(this);
    }
}
