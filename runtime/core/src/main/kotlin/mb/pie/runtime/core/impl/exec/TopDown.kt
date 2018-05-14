package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.TopDownExec
import mb.pie.runtime.core.exec.TopDownExecutor
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled


class TopDownExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UFunc>
) : TopDownExecutor {
  override fun exec() = TopDownExecImpl(store, cache, share, layer.get(), logger.get(), funcs)


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}

open class TopDownExecImpl(
  private val store: Store,
  private val cache: Cache,
  share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>
) : TopDownExec, Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UFuncAppData>()
  private val shared = TopDownExecShared(store, cache, share, layer, logger, visited)


  fun <I : In, O : Out> requireInitial(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecRes<O> {
    try {
      logger.requireTopDownInitialStart(app)
      val info = require(app, cancel)
      logger.requireTopDownInitialEnd(app, info)
      return info
    } finally {
      store.sync()
    }
  }

  override fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled): ExecRes<O> {
    cancel.throwIfCancelled()

    try {
      val resOrData = shared.topdownPrelude(app)
      if(resOrData.res != null) {
        return resOrData.res
      }
      val data = resOrData.data

      // Check if re-execution is necessary.
      if(data == null) {
        // No cached or stored output was found: rebuild
        val reason = NoResultReason()
        val execData = exec(app, reason, cancel, true)
        val res = ExecRes(execData.output.cast<O>(), reason)
        logger.requireTopDownEnd(app, res)
        return res
      }
      val (output, callReqs, pathReqs, pathGens) = data

      // Check for inconsistencies and re-execute when found.
      run {
        // Internal consistency: transient output consistency
        val reason = output.isTransientInconsistent()
        if(reason != null) {
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        }
      }

      // Internal consistency: path requirements
      for(pathReq in pathReqs) {
        logger.checkPathReqStart(app, pathReq)
        val reason = pathReq.checkConsistency()
        if(reason != null) {
          // If a required file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathReqEnd(app, pathReq, reason)
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          logger.checkPathReqEnd(app, pathReq, null)
        }
      }

      // Internal consistency: path generates
      for(pathGen in pathGens) {
        logger.checkPathGenStart(app, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathGenEnd(app, pathGen, reason)
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          logger.checkPathGenEnd(app, pathGen, null)
        }
      }

      // Total consistency: call requirements
      for(callReq in callReqs) {
        val callReqOutput = require(callReq.callee, cancel).output
        logger.checkCallReqStart(app, callReq)
        val reason = callReq.checkConsistency(callReqOutput)
        logger.checkCallReqEnd(app, callReq, reason)
        if(reason != null) {
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validatePostWrite(app, data, it) }
      // Cache and mark as visited
      cache[app] = data
      visited[app] = data
      // Reuse existing result
      val res = ExecRes(output)
      logger.requireTopDownEnd(app, res)
      return res
    } finally {
      layer.requireTopDownEnd(app)
    }
  }

  internal open fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UFuncAppData {
    return shared.exec(app, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun <I : In, O : Out> execInternal(app: FuncApp<I, O>, cancel: Cancelled): UFuncAppData {
    return shared.execInternal(app, cancel, this, this) { _, _ -> }
  }


  override fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, cancel: Cancelled) = requireInitial(app, cancel).output
  override fun <I : In, O : Out> requireResult(app: FuncApp<I, O>, cancel: Cancelled) = requireInitial(app, cancel)
}
