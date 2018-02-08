package mb.pie.runtime.core.exec

import mb.pie.runtime.core.*
import mb.util.async.*


interface TopDownExecutorFactory {
  fun create(store: Store, cache: Cache): TopDownExecutor
}

interface TopDownExecutor : Executor {
  fun exec(): TopDownExec
}

interface TopDownExec {
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): O

  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireInfo(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecInfo<I, O>
}
