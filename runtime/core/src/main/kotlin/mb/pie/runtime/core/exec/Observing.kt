package mb.pie.runtime.core.exec

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.io.Serializable


interface ObservingExecutorFactory {
  fun create(store: Store, cache: Cache): ObservingExecutor
}

// TODO: replace [Serializable]? by [Out], when IntelliJ bug is fixed.
typealias FuncAppObserver = (Serializable?) -> Unit

interface ObservingExecutor : Executor {
  fun setObserver(key: Any, app: UFuncApp, observer: FuncAppObserver)

  fun removeObserver(key: Any)


  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): ExecInfo<I, O>

  @Throws(ExecException::class, InterruptedException::class)
  fun requireBottomUp(changedPaths: List<PPath>, cancel: Cancelled)

  fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean


  fun garbageCollect()
}
