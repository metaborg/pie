package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.ExecReason

@Suppress("DataClassPrivateConstructor")
data class VisitedOrData<I : In, O : Out> private constructor(
  val visited: O?,
  val data: TaskData<I, O>?
) {
  constructor(visited: O) : this(visited, null)
  constructor(data: TaskData<I, O>?) : this(null, data)
}

internal open class TopDownShared(
  private val visited: MutableMap<TaskKey, TaskData<*, *>>,
  private val store: Store,
  private val layer: Layer,
  private val executorLogger: ExecutorLogger
) {
  fun topdownPrelude(key: TaskKey, task: Task<*, *>): VisitedOrData<*, *> {
    Stats.addRequires()
    layer.requireTopDownStart(key, task.input)
    executorLogger.requireTopDownStart(key, task)

    // Check visited cache for output of function application.
    executorLogger.checkVisitedStart(key)
    val visitedData = visited[key]
    if(visitedData != null) {
      // Return output immediately if function application was already visited this execution.
      val visitedOutput = visitedData.output
      executorLogger.checkVisitedEnd(key, visitedOutput)
      executorLogger.requireTopDownEnd(key, task, visitedOutput)
      return VisitedOrData<Nothing, Out>(visitedOutput)
    }
    executorLogger.checkVisitedEnd(key, null)

    val data = existingData(key)
    return VisitedOrData(data)
  }

  private fun existingData(key: TaskKey): TaskData<*, *>? {
    // Check store for output of function application.
    executorLogger.checkStoredStart(key)
    val data = store.readTxn().use { it.data(key) }
    executorLogger.checkStoredEnd(key, data?.output)
    return data
  }
}

/**
 * [Execution reason][ExecReason] for when the transient output of a task is inconsistent.
 */
data class InconsistentTransientOutput(val inconsistentOutput: OutTransient<*>) : ExecReason {
  override fun toString() = "transient output is inconsistent"
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
