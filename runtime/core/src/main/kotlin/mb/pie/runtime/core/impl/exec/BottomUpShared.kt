package mb.pie.runtime.core.impl.exec

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.*


fun directlyAffectedApps(changedFiles: Collection<PPath>, txn: StoreReadTxn, logger: Logger): HashSet<UTask> {
  val directlyAffected = HashSet<UTask>()
  if(changedFiles.isEmpty()) return directlyAffected
  for(changedFile in changedFiles) {
    logger.trace("* file: $changedFile")
    // Check function applications that require the changed file.
    val requireeTasks = txn.requireesOf(changedFile)
    for(requireeTask in requireeTasks) {
      logger.trace("  * required by: ${requireeTask.toShortString(200)}")
      if(!txn.fileReqs(requireeTask).filter { it.file == changedFile }.all { it.isConsistent() }) {
        directlyAffected.add(requireeTask)
      }
    }
    // Check function applications that generate the changed file.
    val generatorTask = txn.generatorOf(changedFile)
    if(generatorTask != null) {
      logger.trace("  * generated by: ${generatorTask.toShortString(200)}")
      if(!txn.fileGens(generatorTask).filter { it.file == changedFile }.all { it.isConsistent() }) {
        directlyAffected.add(generatorTask)
      }
    }
  }
  return directlyAffected
}

fun hasCallReq(caller: UTask, callee: UTask, txn: StoreReadTxn): Boolean {
  // TODO: more efficient implementation for figuring out if an app transitively calls on another app?
  val toCheckQueue: Queue<UTask> = LinkedList()
  toCheckQueue.add(caller)
  while(!toCheckQueue.isEmpty()) {
    val toCheck = toCheckQueue.poll()
    val taskReqs = txn.taskReqs(toCheck);
    if(taskReqs.any { it.calleeEqual(callee) }) {
      return true
    }
    toCheckQueue.addAll(taskReqs.map { it.callee })
  }
  return false
}

class InvalidatedExecReason : ExecReason {
  override fun toString() = "invalidated"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}
