package mb.pie.runtime.exec

import mb.pie.api.*
import java.util.*

class DistinctTaskKeyPriorityQueue(comparator: Comparator<TaskKey>) {
  private val queue = PriorityQueue<TaskKey>(comparator)
  private val set = hashSetOf<TaskKey>()

  companion object {
    fun withTransitiveDependencyComparator(store: Store) = DistinctTaskKeyPriorityQueue(Comparator { key1, key2 ->
      when {
        key1 == key2 -> 0
        store.readTxn().use { txn -> txn.hasTransitiveTaskReq(key1, key2) } -> 1
        else -> -1
      }
    })
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

  fun pollLeastTaskWithDepTo(key: TaskKey, txn: StoreReadTxn): TaskKey? {
    val queueCopy = PriorityQueue(queue)
    while(queueCopy.isNotEmpty()) {
      val queuedKey = queueCopy.poll()
      if(queuedKey == key || txn.hasTransitiveTaskReq(key, queuedKey)) {
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
