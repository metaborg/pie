package mb.pie.runtime.core

import mb.vfs.path.PPath


interface PushingExecutorFactory {
  fun create(store: Store, cache: Cache): PushingExecutor
}

interface PushingExecutor : Executor {
  @Throws(ExecException::class)
  fun require(obsFuncApps: List<AnyObsFuncApp>, changedPaths: List<PPath>)
}

data class ObsFuncApp<out I : In, O : Out>(val app: FuncApp<I, O>, val observer: (O) -> Unit)
typealias UObsFuncApp = ObsFuncApp<*, *>
typealias AnyObsFuncApp = ObsFuncApp<In, Out>
