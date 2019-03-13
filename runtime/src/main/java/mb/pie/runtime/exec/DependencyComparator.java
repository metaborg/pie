package mb.pie.runtime.exec;

import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskKey;

import java.util.Comparator;

public class DependencyComparator implements Comparator<TaskKey> {
    private final Store store;

    public DependencyComparator(Store store) {
        this.store = store;
    }

    @Override
    public int compare(TaskKey key1, TaskKey key2) {
        if(key1.equals(key2)) {
            return 0;
        }

        try(final StoreReadTxn txn = store.readTxn()) {
            if(BottomUpShared.hasTransitiveTaskReq(txn, key1, key2)) {
                return 1;
            }
        }

        return -1;
    }
}