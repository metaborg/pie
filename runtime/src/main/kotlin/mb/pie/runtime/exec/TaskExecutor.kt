package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.exec.ExecReason
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper

class TaskExecutor(
  private val visited: MutableMap<TaskKey, TaskData<*, *>>,
  private val store: Store,
  private val share: Share,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  private val logger: Logger,
  private val executorLogger: ExecutorLogger,
  private val postExecFunc: ((TaskKey, TaskData<*, *>) -> Unit)?
) {
  fun <I : In, O : Out> exec(key: TaskKey, task: Task<I, O>, reason: ExecReason, requireTask: RequireTask, cancel: Cancelled): TaskData<I, O> {
    cancel.throwIfCancelled()
    executorLogger.executeStart(task, reason)
    // OPTO: Inline share functions. Requires statically knowledge of the specific Share type to use.
    val data = share.share(key, { execInternal(key, task, requireTask, cancel) }, { visited[key] })
    executorLogger.executeEnd(task, reason, data)
    return data.cast<I, O>()
  }

  private fun <I : In, O : Out> execInternal(key: TaskKey, task: Task<I, O>, requireTask: RequireTask, cancel: Cancelled): TaskData<I, O> {
    cancel.throwIfCancelled()
    // Execute
    val context = ExecContextImpl(logger, requireTask, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, cancel)
    val output = task.exec(context)
    Stats.addExecution()
    val (callReqs, pathReqs, pathGens) = context.reqs()
    val data = TaskData(task.input, output, callReqs, pathReqs, pathGens)
    // Validate well-formedness of the dependency graph, before writing.
    store.readTxn().use {
      layer.validatePreWrite(key, data, it)
    }
    // Call post-execution function.
    postExecFunc?.invoke(key, data)
    // Write output and dependencies to the store.
    store.writeTxn().use {
      it.setData(key, data)
    }
    // Validate well-formedness of the dependency graph, after writing.
    store.readTxn().use {
      layer.validatePostWrite(key, data, it)
    }
    // Mark as visited.
    visited[key] = data
    return data
  }
}
