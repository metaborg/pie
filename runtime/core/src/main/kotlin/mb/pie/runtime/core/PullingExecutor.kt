package mb.pie.runtime.core


interface PullingExecutorFactory {
  fun create(store: Store, cache: Cache): PullingExecutor
}

interface PullingExecutor : Executor {
  fun newExec(): PullingExec
}

interface PullingExec {
  @Throws(ExecException::class)
  fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>): O

  @Throws(ExecException::class)
  fun <I : In, O : Out> requireInfo(app: FuncApp<I, O>): ExecInfo<I, O>
}
