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
  fun checkResourceRequire(key: TaskKey, task: Task<*, *>, fileReq: ResourceRequireDep): InconsistentResourceRequire? {
    executorLogger.checkResourceRequireStart(key, task, fileReq)
    val reason = fileReq.checkConsistency(resourceSystems)
    executorLogger.checkResourceRequireEnd(key, task, fileReq, reason)
    return reason
  }

  /**
   * Check if a file generates dependency is internally consistent.
   */
  fun checkResourceProvide(key: TaskKey, task: Task<*, *>, fileGen: ResourceProvideDep): InconsistentResourceProvide? {
    executorLogger.checkResourceProvideStart(key, task, fileGen)
    val reason = fileGen.checkConsistency(resourceSystems)
    executorLogger.checkResourceProvideEnd(key, task, fileGen, reason)
    return reason
  }

  /**
   * Check if a task requires dependency is totally consistent.
   */
  fun checkTaskRequire(key: TaskKey, task: Task<*, *>, taskRequire: TaskRequireDep, requireTask: RequireTask, cancel: Cancelled): InconsistentTaskReq? {
    val calleeKey = taskRequire.callee
    val calleeTask = store.readTxn().use { txn -> calleeKey.toTask(taskDefs, txn) }
    val calleeOutput = requireTask.require(calleeKey, calleeTask, cancel)
    executorLogger.checkTaskRequireStart(key, task, taskRequire)
    val reason = taskRequire.checkConsistency(calleeOutput)
    executorLogger.checkTaskRequireEnd(key, task, taskRequire, reason)
    return reason
  }
}
