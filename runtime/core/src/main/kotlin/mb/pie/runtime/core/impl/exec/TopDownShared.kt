package mb.pie.runtime.core.impl.exec

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath

@Suppress("DataClassPrivateConstructor")
data class ResOrData<out O : Out> private constructor(val res: ExecRes<O>?, val data: FuncAppData<O>?) {
  constructor(res: ExecRes<O>) : this(res, null)
  constructor(data: FuncAppData<O>?) : this(null, data)
}

internal open class TopDownExecShared(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val visited: MutableMap<UFuncApp, UFuncAppData>,
  private val generatorOfLocal: MutableMap<PPath, UFuncApp>
) {
  fun <I : In, O : Out> existingData(app: FuncApp<I, O>): FuncAppData<O>? {
    // Check cache for output of function application.
    logger.checkCachedStart(app)
    val cachedData = cache[app]
    logger.checkCachedEnd(app, cachedData?.output)

    // Check store for output of function application.
    return if(cachedData != null) {
      cachedData
    } else {
      logger.checkStoredStart(app)
      val data = store.readTxn().use { it.data(app) }
      logger.checkStoredEnd(app, data?.output)
      data
    }?.cast()
  }

  fun <I : In, O : Out> topdownPrelude(app: FuncApp<I, O>): ResOrData<O> {
    layer.requireTopDownStart(app)
    logger.requireTopDownStart(app)

    // Check visited cache for output of function application.
    logger.checkVisitedStart(app)
    val visitedData = visited[app]?.cast<O>()
    if(visitedData != null) {
      // Return output immediately if function application was already visited this execution.
      logger.checkVisitedEnd(app, visitedData.output)
      val res = ExecRes(visitedData.output)
      logger.requireTopDownEnd(app, res)
      return ResOrData(res)
    }
    logger.checkVisitedEnd(app, null)

    val data = existingData(app)
    return ResOrData(data)
  }


  fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false, execFunc: (FuncApp<I, O>, Cancelled) -> O): ExecRes<O> {
    cancel.throwIfCancelled()
    logger.executeStart(app, reason)
    val output = if(useCache) {
      share.reuseOrCreate(app, { store.readTxn().use { txn -> txn.output(it)?.cast() } }) { execFunc(it, cancel) }
    } else {
      share.reuseOrCreate(app) { execFunc(it, cancel) }
    }
    val result = ExecRes(output, reason)
    logger.executeEnd(app, reason, result)
    return result
  }

  fun <I : In, O : Out> execInternal(app: FuncApp<I, O>, cancel: Cancelled, exec: Exec, funcs: Funcs, writeFunc: (StoreWriteTxn, FuncAppData<O>) -> Unit): O {
    cancel.throwIfCancelled()
    val (id, input) = app
    val builder = funcs.getFunc<I, O>(id)
    val context = ExecContextImpl(exec, store, app, generatorOfLocal, cancel)
    val output = builder.exec(input, context)
    val (callReqs, pathReqs, pathGens) = context.reqs()
    val data = FuncAppData(output, callReqs, pathReqs, pathGens)
    // Validate well-formedness of the dependency graph, before writing.
    store.readTxn().use { layer.validatePostWrite(app, data, funcs, it) }
    // Write output and dependencies to the store.
    store.writeTxn().use { it.setData(app, data); writeFunc(it, data) }
    // Validate well-formedness of the dependency graph, after writing.
    store.readTxn().use { layer.validatePostWrite(app, data, funcs, it) }
    // Cache data
    visited[app] = data
    cache[app] = data
    return output
  }
}