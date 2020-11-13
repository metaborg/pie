package mb.pie.runtime.exec;

import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Comparator;

class DependencyComparator implements Comparator<TaskKey> {
    private final StoreReadTxn txn;

    DependencyComparator(StoreReadTxn txn) {
        this.txn = txn;
    }

    @Override
    public int compare(@NonNull TaskKey key1, @NonNull TaskKey key2) {
        if(key1.equals(key2)) {
            return 0;
        }
        if(BottomUpShared.hasTransitiveTaskReq(key1, key2, txn)) {
            return 1;
        }
        return -1;
    }
}
