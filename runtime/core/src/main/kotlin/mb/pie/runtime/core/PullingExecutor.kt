package mb.pie.runtime.core

import mb.util.async.*


interface PullingExecutorFactory {
  fun create(store: Store, cache: Cache): PullingExecutor
}

interface PullingExecutor : Executor {
  fun newExec(): PullingExec
}

interface PullingExec {
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): O

  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireInfo(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecInfo<I, O>
}
