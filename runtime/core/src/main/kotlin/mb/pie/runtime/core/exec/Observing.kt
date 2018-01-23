package mb.pie.runtime.core.exec

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath


interface ObservingExecutorFactory {
  fun create(store: Store, cache: Cache): ObservingExecutor
}

interface ObservingExecutor : Executor {
  @Throws(ExecException::class, InterruptedException::class)
  fun setObserver(app: UFuncApp, observer: (Out) -> Unit, cancel: Cancelled)

  fun removeObserver(funcApp: UFuncApp)

  @Throws(ExecException::class, InterruptedException::class)
  fun pathsChanged(changedPaths: List<PPath>, cancel: Cancelled)

  fun garbageCollect()
}
