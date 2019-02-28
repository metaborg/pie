package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import java.util.*

/**
 * Returns keys of tasks that are directly affected by changed resources.
 */
public fun directlyAffectedTaskKeys(txn: StoreReadTxn, changedResources: Collection<ResourceKey>, resourceSystems: ResourceSystems, logger: Logger): HashSet<TaskKey> {
  val affected: HashSet<TaskKey> = HashSet<TaskKey>();
  for(changedResource: ResourceKey in changedResources) {
    logger.trace("* resource: " + changedResource);

    val requirees: MutableSet<TaskKey> = txn.requireesOf(changedResource);
    for(key: TaskKey in requirees) {
      logger.trace("  * required by: " + key.toShortString(200));
      if(!txn.resourceRequires(key).filter { dep: ResourceRequireDep -> dep.key == changedResource }.all { dep: ResourceRequireDep -> dep.isConsistent(resourceSystems) }) {
        affected.add(key);
      }
    }

    val provider: TaskKey? = txn.providerOf(changedResource);
    if(provider != null) {
      logger.trace("  * provided by: " + provider.toShortString(200));
      if(!txn.resourceProvides(provider).filter { dep: ResourceProvideDep -> dep.key == changedResource }.all { dep: ResourceProvideDep -> dep.isConsistent(resourceSystems) }) {
        affected.add(provider);
      }
    }
  }

  return affected;
}

/**
 * Checks whether [caller] has a transitive (or direct) task requirement to [callee].
 */
public fun hasTransitiveTaskReq(txn: StoreReadTxn, caller: TaskKey, callee: TaskKey): Boolean {
  // TODO: more efficient implementation for figuring out if an app transitively calls on another app?
  val toCheckQueue: Queue<TaskKey> = LinkedList();
  toCheckQueue.add(caller);
  while(!toCheckQueue.isEmpty()) {
    val toCheck: TaskKey = toCheckQueue.poll();
    val taskReqs: MutableList<TaskRequireDep> = txn.taskRequires(toCheck);
    if(taskReqs.any { dep: TaskRequireDep -> dep.calleeEqual(callee) }) {
      return true;
    }
    toCheckQueue.addAll(taskReqs.map { dep: TaskRequireDep -> dep.callee });
  }
  return false;
}

/**
 * [Execution reason][ExecReason] for when a task is (directly or indirectly) affected by a change.
 */
public class AffectedExecReason : ExecReason {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true;
    if(other != null && other.javaClass != javaClass) return false;
    return true;
  }

  override fun hashCode(): Int {
    return 0;
  }

  override fun toString(): String {
    return "directly or indirectly affected by change";
  }
}
