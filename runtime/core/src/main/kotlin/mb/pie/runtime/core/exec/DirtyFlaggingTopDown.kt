package mb.pie.runtime.core.exec

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
import mb.vfs.path.PPath


interface DirtyFlaggingTopDownExecutorFactory {
  fun create(store: Store, cache: Cache): DirtyFlaggingTopDownExecutor
}

interface DirtyFlaggingTopDownExecutor : Executor {
  fun add(key: Any, obsFuncApp: AnyObsFuncApp)
  fun update(key: Any, obsFuncApp: AnyObsFuncApp)
  fun remove(key: Any)

  fun pathChanged(path: PPath)
  fun pathsChanged(paths: Collection<PPath>)
  fun dirtyFlag()

  @Throws(ExecException::class, InterruptedException::class)
  fun executeAll(cancel: Cancelled = NullCancelled())

  @Throws(ExecException::class, InterruptedException::class)
  fun addAndExecute(key: Any, obsFuncApp: AnyObsFuncApp, cancel: Cancelled = NullCancelled())

  @Throws(ExecException::class, InterruptedException::class)
  fun updateAndExecute(key: Any, obsFuncApp: AnyObsFuncApp, cancel: Cancelled = NullCancelled())
}

data class ObsFuncApp<out I : In, O : Out>(val app: FuncApp<I, O>, val observer: (O) -> Unit)
typealias UObsFuncApp = ObsFuncApp<*, *>
typealias AnyObsFuncApp = ObsFuncApp<In, Out>
