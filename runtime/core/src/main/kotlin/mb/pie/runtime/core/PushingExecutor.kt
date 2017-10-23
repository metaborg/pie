package mb.pie.runtime.core

import mb.vfs.path.PPath


interface PushingExecutorFactory {
  fun create(store: Store, cache: Cache): PushingExecutor
}

interface PushingExecutor : Executor {
  @Throws(ExecException::class)
  fun require(obsFuncs: List<AnyObsFunc>, changedPaths: List<PPath>)
}

data class ObsFunc<out I : In, O : Out>(val app: FuncApp<I, O>, val changedFunc: (O) -> Unit)
typealias UObsFunc = ObsFunc<*, *>
typealias AnyObsFunc = ObsFunc<In, Out>
