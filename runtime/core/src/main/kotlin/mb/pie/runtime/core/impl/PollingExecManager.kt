package mb.pie.runtime.core.impl

import com.google.inject.*
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*


class PollingExecSessionImpl(private val build: PollingExec, private val injector: Injector)
  : PollingExecSession {
  override fun <I : In, O : Out> exec(app: FuncApp<I, O>) =
    build.requireInitial(app).result.output

  override fun <I : In, O : Out, B : Func<I, O>> exec(clazz: Class<B>, input: I) =
    build.requireInitial(FuncApp(injector.getInstance(clazz), input)).result.output

  override fun <I : In, O : Out> execToInfo(app: FuncApp<I, O>) =
    build.requireInitial(app)

  override fun <I : In, O : Out, B : Func<I, O>> execToInfo(clazz: Class<B>, input: I) =
    build.requireInitial(FuncApp(injector.getInstance(clazz), input))
}

class PollingExecManagerImpl @Inject constructor(
  private @Assisted val store: Store,
  private @Assisted val cache: Cache,
  private val share: BuildShare,
  private val layer: Provider<Layer>,
  private val funcs: MutableMap<String, UFunc>,
  private val injector: Injector)
  : PollingExecManager {
  override fun newSession(): PollingExecSession {
    val reporter = injector.getInstance(Logger::class.java)
    return PollingExecSessionImpl(PollingExec(store, cache, share, layer.get(), reporter, funcs, injector), injector)
  }

  override fun dropStore() = store.writeTxn().use { it.drop() }
  override fun dropCache() = cache.drop()

  override fun <I : In, O : Out> exec(app: FuncApp<I, O>) =
    newSession().exec(app)

  override fun <I : In, O : Out, B : Func<I, O>> exec(clazz: Class<B>, input: I) =
    newSession().exec(clazz, input)

  override fun <I : In, O : Out> execToInfo(app: FuncApp<I, O>) =
    newSession().execToInfo(app)

  override fun <I : In, O : Out, B : Func<I, O>> execToInfo(clazz: Class<B>, input: I) =
    newSession().execToInfo(clazz, input)
}

