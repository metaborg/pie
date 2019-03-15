package mb.pie.api.exec;

/**
 * Executor using a top-down build algorithm.
 */
public interface TopDownExecutor {
    /**
     * Creates a new top-down build session. Within a session, the same task is never executed more than once. For sound incrementality, a
     * new session must be started after external changes (such as file changes) have occurred.
     */
    TopDownSession newSession();
}
