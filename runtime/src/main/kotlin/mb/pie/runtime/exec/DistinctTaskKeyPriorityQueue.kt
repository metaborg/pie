package mb.pie.runtime.exec

import mb.pie.api.Store
import mb.pie.api.TaskKey
import java.util.*

class DistinctTaskKeyPriorityQueue(comparator: Comparator<TaskKey>) {
  private val queue = PriorityQueue<TaskKey>(comparator)
  private val set = hashSetOf<TaskKey>()

  companion object {
    fun withTransitiveDependencyComparator(store: Store) = DistinctTaskKeyPriorityQueue(DependencyComparator(store))
  }

  fun isNotEmpty(): Boolean {
    return queue.isNotEmpty()
  }

  fun contains(key: TaskKey): Boolean {
    return set.contains(key)
  }

  fun poll(): TaskKey {
    val key = queue.remove()
    set.remove(key)
    return key
  }

  fun pollLeastTaskWithDepTo(key: TaskKey, store: Store): TaskKey? {
    val queueCopy = PriorityQueue(queue)
    while(queueCopy.isNotEmpty()) {
      val queuedKey = queueCopy.poll()
      if(queuedKey == key || store.readTxn().use { txn -> txn.hasTransitiveTaskReq(key, queuedKey) }) {
        queue.remove(queuedKey)
        set.remove(queuedKey)
        return queuedKey
      }
    }
    return null
  }

  fun add(key: TaskKey) {
    if(set.contains(key)) return
    queue.add(key)
    set.add(key)
  }

  override fun toString() = queue.toString()
}

class DependencyComparator(private val store: Store) : Comparator<TaskKey> {
  override fun compare(key1: TaskKey, key2: TaskKey): Int {
    return when {
      key1 == key2 -> 0
      store.readTxn().use { txn -> txn.hasTransitiveTaskReq(key1, key2) } -> 1
      else -> -1
    }
  }
}