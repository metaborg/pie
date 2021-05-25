package mb.pie.api;

import java.io.Serializable;

/**
 * The observability status of a task.
 */
public enum Observability implements Serializable {
    /**
     * An explicitly observed task is one that has been explicitly required by the user of the system. It is observed
     * even if no other tasks observes it.
     */
    ExplicitObserved,
    /**
     * An implicitly observed task is one that is transitively observed by a {@link #ExplicitObserved} task.
     */
    ImplicitObserved,
    /**
     * An unobserved task is one where no other task observes it. This can either occur implicitly, when a task no
     * longer requires another task, which is not observed by any other task; or explicitly, when the user of the system
     * indicates that a task, which is not observed by any other task, is no longer required.
     */
    Unobserved;


    /**
     * @return True when the observability status is {@link #ExplicitObserved} or {@link #ImplicitObserved}. False
     * otherwise.
     */
    public boolean isObserved() {
        return this == ExplicitObserved || this == ImplicitObserved;
    }

    /**
     * @return True when the observability status is {@link #Unobserved}. False otherwise.
     */
    public boolean isUnobserved() {
        return this == Unobserved;
    }


    /**
     * Explicitly unobserves task with given {@code key}, settings its observability status to {@link
     * Observability#ImplicitObserved} if it was {@link Observability#ExplicitObserved} but still observed by another
     * task. Otherwise, sets the observability status to {@link Observability#Unobserved} and then propagates this to
     * required tasks.
     *
     * @param txn Store write transaction.
     * @param key Key of the task to unobsreve.
     */
    public static void explicitUnobserve(StoreWriteTxn txn, TaskKey key, Tracer tracer) {
        final Observability previousObservability = txn.taskObservability(key);
        if(previousObservability == Observability.Unobserved) {
            // If task is already unobserved, there is no need to do anything.
            return;
        }
        if(isObservedByCaller(txn, key)) {
            if(previousObservability == Observability.ExplicitObserved) {
                // Task is explicitly observed, but also implicitly observed by a caller. Therefore we set the
                // observability to ImplicitObserved.
                final Observability newObservability = Observability.ImplicitObserved;
                tracer.setTaskObservability(key, previousObservability, newObservability);
                txn.setTaskObservability(key, newObservability);
            }
        } else {
            final Observability newObservability = Observability.Unobserved;
            tracer.setTaskObservability(key, previousObservability, newObservability);
            txn.setTaskObservability(key, newObservability);
            for(TaskRequireDep taskRequire : txn.taskRequires(key)) {
                implicitUnobserve(txn, taskRequire.callee, tracer);
            }
        }
    }

    /**
     * Implicitly unobserves task with given {@code key}, settings its observability status to {@link
     * Observability#Unobserved} if it was not already unobserved and if no other task observes it. Then propagates this
     * to required tasks.
     *
     * @param txn Store write transaction.
     * @param key Key of the task to unobserve.
     */
    public static void implicitUnobserve(StoreWriteTxn txn, TaskKey key, Tracer tracer) {
        final Observability previousObservability = txn.taskObservability(key);
        if(previousObservability != Observability.ImplicitObserved) {
            // If task is already unobserved, there is no need to do anything.
            // If task is explicitly observed, we may not implicitly unobserve it, so we stop.
            return;
        }
        if(isObservedByCaller(txn, key)) {
            return; // Cannot unobserve, an observed task requires the task.
        }
        final Observability newObservability = Observability.Unobserved;
        tracer.setTaskObservability(key, previousObservability, newObservability);
        txn.setTaskObservability(key, newObservability);
        for(TaskRequireDep taskRequire : txn.taskRequires(key)) {
            implicitUnobserve(txn, taskRequire.callee, tracer);
        }
    }


    private static boolean isObservedByCaller(StoreReadTxn txn, TaskKey key) {
        return txn.callersOf(key).stream().map(txn::taskObservability).anyMatch(Observability::isObserved);
    }
}
