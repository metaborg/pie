package mb.pie.runtime.core.impl.exec

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled


@Suppress("DataClassPrivateConstructor")
data class VisitedOrData<out O : Out> private constructor(val visited: O?, val data: TaskData<O>?) {
  constructor(visited: O) : this(visited, null)
  constructor(data: TaskData<O>?) : this(null, data)
}

internal open class TopDownExecShared(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val visited: MutableMap<UTask, UTaskData>
) {
  private fun <I : In, O : Out> existingData(task: Task<I, O>): TaskData<O>? {
    // Check cache for output of function application.
    logger.checkCachedStart(task)
    val cachedData = cache[task]
    logger.checkCachedEnd(task, cachedData?.output)

    // Check store for output of function application.
    return if(cachedData != null) {
      cachedData
    } else {
      logger.checkStoredStart(task)
      val data = store.readTxn().use { it.data(task) }
      logger.checkStoredEnd(task, data?.output)
      data
    }?.cast<O>()
  }

  fun <I : In, O : Out> topdownPrelude(task: Task<I, O>): VisitedOrData<O> {
    Stats.addRequires()
    layer.requireTopDownStart(task)
    logger.requireTopDownStart(task)

    // Check visited cache for output of function application.
    logger.checkVisitedStart(task)
    val visitedData = visited[task]?.cast<O>()
    if(visitedData != null) {
      // Return output immediately if function application was already visited this execution.
      val visitedOutput = visitedData.output
      logger.checkVisitedEnd(task, visitedOutput)
      logger.requireTopDownEnd(task, visitedOutput)
      return VisitedOrData(visitedOutput)
    }
    logger.checkVisitedEnd(task, null)

    val data = existingData(task)
    return VisitedOrData(data)
  }


  fun exec(task: UTask, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false, execFunc: (UTask, Cancelled) -> UTaskData): UTaskData {
    cancel.throwIfCancelled()
    logger.executeStart(task, reason)
    val data = if(useCache) {
      share.reuseOrCreate(task, { store.readTxn().use { txn -> txn.data(it) } }) { execFunc(it, cancel) }
    } else {
      share.reuseOrCreate(task) { execFunc(it, cancel) }
    }
    logger.executeEnd(task, reason, data)
    return data
  }

  fun execInternal(task: UTask, cancel: Cancelled, requireTask: RequireTask, taskDefs: TaskDefs, writeFunc: (StoreWriteTxn, UTaskData) -> Unit): UTaskData {
    cancel.throwIfCancelled()
    val (id, input) = task
    val builder = taskDefs.getGTaskDef(id)
    val context = ExecContextImpl(requireTask, cancel)
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
