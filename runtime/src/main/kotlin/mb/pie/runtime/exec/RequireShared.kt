package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.Cancelled
import mb.pie.api.exec.NullCancelled

interface RequireTask {
  fun <I : In, O : Out> require(key: TaskKey, task: Task<I, O>, cancel: Cancelled = NullCancelled()): O
}

internal class RequireShared(
  private val taskDefs: TaskDefs,
  private val resourceSystems: ResourceSystems,
  private val visited: MutableMap<TaskKey, TaskData<*, *>>,
  private val store: Store,
  private val executorLogger: ExecutorLogger
) {
  /**
   * Attempt to get task data from the visited cache.
   */
  fun dataFromVisited(key: TaskKey): TaskData<*, *>? {
    executorLogger.checkVisitedStart(key)
    val data = visited[key]
    executorLogger.checkVisitedEnd(key, data?.output)
    return data
  }

  /**
   * Attempt to get task data from the store.
   */
  fun dataFromStore(key: TaskKey): TaskData<*, *>? {
    executorLogger.checkStoredStart(key)
    val data = store.readTxn().use { it.data(key) }
    executorLogger.checkStoredEnd(key, data?.output)
    return data
  }


  /**
   * Check if input is internally consistent.
   */
  fun <I : In> checkInput(input: I, task: Task<I, *>): InconsistentInput? {
    if(input != task.input) {
      return InconsistentInput(input, task.input)
    }
    return null
  }

  /**
   * Check if output is internally consistent.
   */
  fun checkOutputConsistency(output: Out): InconsistentTransientOutput? {
    return output.isTransientInconsistent()
  }

  /**
   * Check if a file requires dependency is internally consistent.
   */
  fun checkFileReq(key: TaskKey, task: Task<*, *>, fileReq: ResourceRequire): InconsistentResourceRequire? {
    executorLogger.checkFileReqStart(key, task, fileReq)
    val reason = fileReq.checkConsistency(resourceSystems)
    executorLogger.checkFileReqEnd(key, task, fileReq, reason)
    return reason
  }

  /**
   * Check if a file generates dependency is internally consistent.
   */
  fun checkFileGen(key: TaskKey, task: Task<*, *>, fileGen: ResourceProvide): InconsistentResourceProvide? {
    executorLogger.checkFileGenStart(key, task, fileGen)
    val reason = fileGen.checkConsistency(resourceSystems)
    executorLogger.checkFileGenEnd(key, task, fileGen, reason)
    return reason
  }

  /**
   * Check if a task requires dependency is totally consistent.
   */
  fun checkTaskReq(key: TaskKey, task: Task<*, *>, taskReq: TaskReq, requireTask: RequireTask, cancel: Cancelled): InconsistentTaskReq? {
    val calleeKey = taskReq.callee
    val calleeTask = store.readTxn().use { txn -> calleeKey.toTask(taskDefs, txn) }
    val calleeOutput = requireTask.require(calleeKey, calleeTask, cancel)
    executorLogger.checkTaskReqStart(key, task, taskReq)
    val reason = taskReq.checkConsistency(calleeOutput)
    executorLogger.checkTaskReqEnd(key, task, taskReq, reason)
    return reason
  }
}
