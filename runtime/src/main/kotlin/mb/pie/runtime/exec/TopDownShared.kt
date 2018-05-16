package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.util.async.Cancelled

@Suppress("DataClassPrivateConstructor")
data class VisitedOrData<out O : Out> private constructor(
  val visited: O?,
  val data: TaskData<O>?
) {
  constructor(visited: O) : this(visited, null)
  constructor(data: TaskData<O>?) : this(null, data)
}

internal open class TopDownExecShared(
  private val taskDefs: TaskDefs,
  private val visited: MutableMap<UTask, UTaskData>,
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  private val logger: Logger,
  private val executorLogger: ExecutorLogger
) {
  private fun <I : In, O : Out> existingData(task: Task<I, O>): TaskData<O>? {
    // Check cache for output of function application.
    executorLogger.checkCachedStart(task)
    val cachedData = cache[task]
    executorLogger.checkCachedEnd(task, cachedData?.output)

    // Check store for output of function application.
    return if(cachedData != null) {
      cachedData
    } else {
      executorLogger.checkStoredStart(task)
      val data = store.readTxn().use { it.data(task) }
      executorLogger.checkStoredEnd(task, data?.output)
      data
    }?.cast<O>()
  }

  fun <I : In, O : Out> topdownPrelude(task: Task<I, O>): VisitedOrData<O> {
    Stats.addRequires()
    layer.requireTopDownStart(task)
    executorLogger.requireTopDownStart(task)

    // Check visited cache for output of function application.
    executorLogger.checkVisitedStart(task)
    val visitedData = visited[task]?.cast<O>()
    if(visitedData != null) {
      // Return output immediately if function application was already visited this execution.
      val visitedOutput = visitedData.output
      executorLogger.checkVisitedEnd(task, visitedOutput)
      executorLogger.requireTopDownEnd(task, visitedOutput)
      return VisitedOrData(visitedOutput)
    }
    executorLogger.checkVisitedEnd(task, null)

    val data = existingData(task)
    return VisitedOrData(data)
  }


  fun exec(task: UTask, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false, execFunc: (UTask, Cancelled) -> UTaskData): UTaskData {
    cancel.throwIfCancelled()
    executorLogger.executeStart(task, reason)
    val data = if(useCache) {
      share.reuseOrCreate(task, { store.readTxn().use { txn -> txn.data(it) } }) { execFunc(it, cancel) }
    } else {
      share.reuseOrCreate(task) { execFunc(it, cancel) }
    }
    executorLogger.executeEnd(task, reason, data)
    return data
  }

  fun execInternal(task: UTask, cancel: Cancelled, requireTask: RequireTask, writeFunc: (StoreWriteTxn, UTaskData) -> Unit): UTaskData {
    cancel.throwIfCancelled()
    val (id, input) = task
    val builder = taskDefs.getGTaskDef(id)
    val context = ExecContextImpl(logger, requireTask, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, cancel)
    val output = builder.execUntyped(input, context)
    Stats.addExecution()
    val (callReqs, pathReqs, pathGens) = context.reqs()
    val data = TaskData(output, callReqs, pathReqs, pathGens)

    // Validate well-formedness of the dependency graph, before writing.
    store.readTxn().use { layer.validatePreWrite(task, data, it) }
    // Write output and dependencies to the store.
    store.writeTxn().use { it.setData(task, data); writeFunc(it, data) }
    // Validate well-formedness of the dependency graph, after writing.
    store.readTxn().use { layer.validatePostWrite(task, data, it) }

    // Cache data
    visited[task] = data
    cache[task] = data
    return data
  }
}


private fun TaskDef<In, *>.execUntyped(input: In, ctx: ExecContext): Out = ctx.exec(input.cast())


class NoResultReason : ExecReason {
  override fun toString() = "no stored or cached output"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}


/**
 * @return an [execution reason][ExecReason] when this output is transient and not consistent, `null` otherwise.
 */
fun Out.isTransientInconsistent(): ExecReason? {
  return when(this) {
    is OutTransient<*> -> when {
      this.consistent -> null
      else -> InconsistentTransientOutput(this)
    }
    else -> null
  }
}

data class InconsistentTransientOutput(val inconsistentOutput: OutTransient<*>) : ExecReason {
  override fun toString() = "transient output is inconsistent"
}
