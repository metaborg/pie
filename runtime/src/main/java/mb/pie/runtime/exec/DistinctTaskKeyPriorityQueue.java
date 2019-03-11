package mb.pie.runtime.exec;

import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DistinctTaskKeyPriorityQueue {
    private final PriorityQueue<TaskKey> queue;
    private final HashSet<TaskKey> set;


    public DistinctTaskKeyPriorityQueue(Comparator<TaskKey> comparator) {
        this.queue = new PriorityQueue<>(comparator);
        this.set = new HashSet<>();
    }

    public static DistinctTaskKeyPriorityQueue withTransitiveDependencyComparator(Store store) {
        return new DistinctTaskKeyPriorityQueue(new DependencyComparator(store));
    }


    public boolean isNotEmpty() {
        return !queue.isEmpty();
    }

    public boolean contains(TaskKey key) {
        return set.contains(key);
    }

    public TaskKey poll() {
        final TaskKey key = queue.remove();
        set.remove(key);
        return key;
    }

    public @Nullable TaskKey pollLeastTaskWithDepTo(TaskKey key, Store store) {
        final PriorityQueue<TaskKey> queueCopy = new PriorityQueue<>(queue);
        while(!queueCopy.isEmpty()) {
            final TaskKey queuedKey = queueCopy.poll();
            if(queuedKey.equals(key)) {
                queue.remove(queuedKey);
                set.remove(queuedKey);
                return queuedKey;
            }
            // Repeat to skip transaction in case queuedKey equals key.
            try(final StoreReadTxn txn = store.readTxn()) {
                if(BottomUpShared.hasTransitiveTaskReq(txn, key, queuedKey)) {
                    queue.remove(queuedKey);
                    set.remove(queuedKey);
                    return queuedKey;
                }
            }
        }
        return null;
    }

    public void add(TaskKey key) {
        if(set.contains(key)) return;
        queue.add(key);
        set.add(key);
    }

    @Override public String toString() {
        return queue.toString();
    }
}

