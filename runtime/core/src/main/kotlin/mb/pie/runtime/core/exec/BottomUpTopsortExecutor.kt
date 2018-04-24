package mb.pie.runtime.core.exec

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.io.Serializable


interface BottomUpTopsortExecutorFactory {
  fun create(store: Store, cache: Cache): BottomUpTopsortExecutor
}

interface BottomUpTopsortExecutor : Executor {
  fun setObserver(key: Any, app: UFuncApp, observer: FuncAppObserver)

  fun removeObserver(key: Any)


  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): O

  @Throws(ExecException::class, InterruptedException::class)
  fun requireBottomUp(changedPaths: Set<PPath>, cancel: Cancelled)

  fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean
}
