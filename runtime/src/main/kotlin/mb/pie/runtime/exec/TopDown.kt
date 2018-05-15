package mb.pie.runtime.exec

import mb.pie.runtime.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled


interface TopDownExecutorFactory {
  fun create(store: Store, cache: Cache): TopDownExecutor
}

interface TopDownExecutor : Executor {
  fun exec(): TopDownExec
}

interface TopDownExec {
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireInitial(task: Task<I, O>, cancel: Cancelled = NullCancelled()): O
}
