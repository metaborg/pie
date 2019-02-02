package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import java.util.*

/**
 * Returns keys of tasks that are directly affected by changed resources.
 */
fun StoreReadTxn.directlyAffectedTaskKeys(changedResources: Collection<ResourceKey>, resourceSystems: ResourceSystems, logger: Logger): HashSet<TaskKey> {
  val affected = HashSet<TaskKey>()
  for(changedResource in changedResources) {
    logger.trace("* resource: $changedResource")

    val requirees = requireesOf(changedResource)
    for(key in requirees) {
      logger.trace("  * required by: ${key.toShortString(200)}")
      if( observability(key).isNotObservable() ) {
        logger.trace("  * Is Detached ")
      } else if(!resourceRequires(key).filter { it.key == changedResource }.all { it.isConsistent(resourceSystems) }) {
        affected.add(key)
      }
    }

    val provider = providerOf(changedResource)
    if(provider != null) {
      logger.trace("  * provided by: ${provider.toShortString(200)}")
      if(!resourceProvides(provider).filter { it.key == changedResource }.all { it.isConsistent(resourceSystems) }) {
        affected.add(provider)
      }
    }
  }

  return affected
}

/**
 * Checks whether [caller] has a transitive (or direct) task requirement to [callee].
 */
fun StoreReadTxn.hasTransitiveTaskReq(caller: TaskKey, callee: TaskKey): Boolean {
  // TODO: more efficient implementation for figuring out if an app transitively calls on another app?
  val toCheckQueue: Queue<TaskKey> = LinkedList()
  toCheckQueue.add(caller)
  while(!toCheckQueue.isEmpty()) {
    val toCheck = toCheckQueue.poll()
    val taskReqs = taskRequires(toCheck);
    if(taskReqs.any { it.calleeEqual(callee) }) {
      return true
    }
    toCheckQueue.addAll(taskReqs.map { it.callee })
  }
  return false
}

/**
 * [Execution reason][ExecReason] for when a task is (directly or indirectly) affected by a change.
 */
class AffectedExecReason : ExecReason {
  override fun toString() = "directly or indirectly affected by change"

  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}
