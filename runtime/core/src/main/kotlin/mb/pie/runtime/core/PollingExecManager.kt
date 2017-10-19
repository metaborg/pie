package mb.pie.runtime.core


interface PollingExecSession {
  @Throws(ExecException::class)
  fun <I : In, O : Out> exec(app: FuncApp<I, O>): O

  @Throws(ExecException::class)
  fun <I : In, O : Out, B : Func<I, O>> exec(clazz: Class<B>, input: I): O

  @Throws(ExecException::class)
  fun <I : In, O : Out> execToInfo(app: FuncApp<I, O>): ExecInfo<I, O>

  @Throws(ExecException::class)
  fun <I : In, O : Out, B : Func<I, O>> execToInfo(clazz: Class<B>, input: I): ExecInfo<I, O>
}

@Throws(ExecException::class)
inline fun <I : In, O : Out, reified B : Func<I, O>> PollingExecSession.exec(input: I): O {
  return this.exec(B::class.java, input)
}

@Throws(ExecException::class)
inline fun <I : In, O : Out, reified B : Func<I, O>> PollingExecSession.execToInfo(input: I): ExecInfo<I, O> {
  return this.execToInfo(B::class.java, input)
}


interface PollingExecManager : PollingExecSession {
  fun newSession(): PollingExecSession

  fun dropStore()
  fun dropCache()
}

interface PollingExecManagerFactory {
  fun create(store: Store, cache: Cache): PollingExecManager
}
