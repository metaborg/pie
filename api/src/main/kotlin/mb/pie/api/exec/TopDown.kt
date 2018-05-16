package mb.pie.api.exec

import mb.pie.api.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled

/**
 * Executor using a top-down build algorithm.
 */
interface TopDownExecutor {
  /**
   * Creates a new top-down build session. Within a session, the same task is never executed more than once. For sound incrementality, a
   * new session must be started after external changes (such as file changes) have occurred.
   */
  fun newSession(): TopDownSession
}

interface TopDownSession {
  /**
   * Requires given task, returning its output.
   */
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireInitial(task: Task<I, O>, cancel: Cancelled = NullCancelled()): O
}
