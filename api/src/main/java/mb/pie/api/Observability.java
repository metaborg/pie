package mb.pie.api;

import java.io.Serializable;

/**
 * The observability status of a task.
 */
public enum Observability implements Serializable {
    /**
     * A root observed task is one that has been explicitly required by the user of the system. It is observed even if
     * no other tasks observes it.
     */
    RootObserved,
    /**
     * A transitively observed task is one that is transitively observed by a {@link #RootObserved} task.
     */
    TransitivelyObserved,
    /**
     * An unobserved task is one where no other task observes it. This can either occur implicitly, when a task no
     * longer requires another task, which is not observed by any other task; or explicitly, when the user of the system
     * indicates that a task, which is not observed by any other task, is no longer required.
     */
    Unobserved;


    /**
     * @return True when the observability status is {@link #RootObserved} or {@link #TransitivelyObserved}. False
     * otherwise.
     */
    public boolean isObserved() {
        return this == RootObserved || this == TransitivelyObserved;
    }

    /**
     * @return True when the observability status is {@link #Unobserved}. False otherwise.
     */
    public boolean isUnobserved() {
        return this == Unobserved;
    }


    /**
     * Explicitly unobserves task with given {@code key}, settings its observability status to {@link
     * Observability#TransitivelyObserved} if it was {@link Observability#RootObserved} but still observed by another
     * task. Otherwise, sets the observability status to {@link Observability#Unobserved} and then propagates this to
     * required tasks.
     *
     * @param txn Store write transaction.
     * @param key Key of the task to unobsreve.
     */
    public static void explicitUnobserve(StoreWriteTxn txn, TaskKey key) {
        if(isObservedByCaller(txn, key)) {
            // Task is observed, therefore we cannot detach it. If the task was RootObserved, we set it to Observed.
            txn.setTaskObservability(key, Observability.TransitivelyObserved);
        } else {
            txn.setTaskObservability(key, Observability.Unobserved);
            for(TaskRequireDep taskRequire : txn.taskRequires(key)) {
                implicitUnobserve(txn, taskRequire.callee);
            }
        }
    }

    /**
     * Implicitly unobserves task with given {@code key}, settings its observability status to {@link
     * Observability#Unobserved} if it was not already detached and if no other task observes it. Then propagates this
     * to required tasks.
     *
     * @param txn Store write transaction.
     * @param key Key of the task to detach.
     */
    public static void implicitUnobserve(StoreWriteTxn txn, TaskKey key) {
        if(txn.taskObservability(key).isUnobserved()) {
            return; // Already detached, no need to propagate.
        }
        if(isObservedByCaller(txn, key)) {
            return; // Cannot detach, an observed task requires the task.
        }
        txn.setTaskObservability(key, Observability.Unobserved);
        for(TaskRequireDep taskRequire : txn.taskRequires(key)) {
            implicitUnobserve(txn, taskRequire.callee);
        }
    }


    private static boolean isObservedByCaller(StoreReadTxn txn, TaskKey key) {
        return txn.callersOf(key).stream().map(txn::taskObservability).anyMatch(Observability::isObserved);
    }
}
