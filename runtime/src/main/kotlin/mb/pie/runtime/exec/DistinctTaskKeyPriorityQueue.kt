package mb.pie.runtime.exec

import mb.pie.api.*
import java.util.*

public class DistinctTaskKeyPriorityQueue {
  private val queue: PriorityQueue<TaskKey>;
  private val set: HashSet<TaskKey>;


  public constructor(comparator: Comparator<TaskKey>) {
    this.queue = PriorityQueue<TaskKey>(comparator);
    this.set = hashSetOf<TaskKey>();
  }

  companion object {
    fun withTransitiveDependencyComparator(store: Store): DistinctTaskKeyPriorityQueue {
      return DistinctTaskKeyPriorityQueue(DependencyComparator(store));
    }
  }


  fun isNotEmpty(): Boolean {
    return !queue.isEmpty();
  }

  fun contains(key: TaskKey): Boolean {
    return set.contains(key);
  }

  fun poll(): TaskKey {
    val key: TaskKey = queue.remove();
    set.remove(key);
    return key;
  }

  fun pollLeastTaskWithDepTo(key: TaskKey, store: Store): TaskKey? {
    val queueCopy: PriorityQueue<TaskKey> = PriorityQueue(queue);
    while(!queueCopy.isEmpty()) {
      val queuedKey: TaskKey = queueCopy.poll();
      if(queuedKey.equals(key) || store.readTxn().use { txn: StoreReadTxn -> hasTransitiveTaskReq(txn, key, queuedKey) }) {
        queue.remove(queuedKey);
        set.remove(queuedKey);
        return queuedKey;
      }
    }
    return null;
  }

  fun add(key: TaskKey) {
    if(set.contains(key)) return;
    queue.add(key);
    set.add(key);
  }

  override fun toString(): String {
    return queue.toString();
  }
}

public class DependencyComparator : Comparator<TaskKey> {
  private val store: Store;

  public constructor(store: Store) {
    this.store = store;
  }

  override fun compare(key1: TaskKey, key2: TaskKey): Int {
    if(key1.equals(key2)) {
      return 0;
    } else if(store.readTxn().use { txn: StoreReadTxn -> hasTransitiveTaskReq(txn, key1, key2) }) {
      return 1;
    } else {
      return -1;
    }
  }
}