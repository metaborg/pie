package mb.pie.runtime.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.*
import mb.pie.runtime.exec.TopDownExec
import mb.pie.runtime.exec.TopDownExecutor
import mb.pie.runtime.impl.*
import mb.util.async.Cancelled


class TopDownExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UTaskDef>
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
  private val funcs: Map<String, UTaskDef>
) : TopDownExec, RequireTask, TaskDefs by TaskDefsImpl(funcs) {
  private val visited = mutableMapOf<UTask, UTaskData>()
  private val shared = TopDownExecShared(store, cache, share, layer, logger, visited)


  override fun <I : In, O : Out> requireInitial(task: Task<I, O>, cancel: Cancelled): O {
    try {
      logger.requireTopDownInitialStart(task)
      val info = require(task, cancel)
      logger.requireTopDownInitialEnd(task, info)
      return info
    } finally {
      store.sync()
    }
  }

  override fun <I : In, O : Out> require(task: Task<I, O>, cancel: Cancelled): O {
    cancel.throwIfCancelled()

    try {
      val outputOrData = shared.topdownPrelude(task)
      if(outputOrData.visited != null) {
        return outputOrData.visited
      }
      val data = outputOrData.data

      // Check if re-execution is necessary.
      if(data == null) {
        // No cached or stored output was found: rebuild
        val reason = NoResultReason()
        val execData = exec(task, reason, cancel, true)
        val output = execData.output.cast<O>()
        logger.requireTopDownEnd(task, output)
        return output
      }
      val (existingOutput, callReqs, pathReqs, pathGens) = data

      // Check for inconsistencies and re-execute when found.
      run {
        // Internal consistency: transient output consistency
        val reason = existingOutput.isTransientInconsistent()
        if(reason != null) {
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          logger.requireTopDownEnd(task, execOutput)
          return execOutput
        }
      }

      // Internal consistency: file requirements
      for(pathReq in pathReqs) {
        logger.checkFileReqStart(task, pathReq)
        val reason = pathReq.checkConsistency()
        if(reason != null) {
          // If a required file is outdated (i.e., its stamp changed): rebuild
          logger.checkFileReqEnd(task, pathReq, reason)
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          logger.requireTopDownEnd(task, execOutput)
          return execOutput
        } else {
          logger.checkFileReqEnd(task, pathReq, null)
        }
      }

      // Internal consistency: file generates
      for(pathGen in pathGens) {
        logger.checkFileGenStart(task, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          logger.checkFileGenEnd(task, pathGen, reason)
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          logger.requireTopDownEnd(task, execOutput)
          return execOutput
        } else {
          logger.checkFileGenEnd(task, pathGen, null)
        }
      }

      // Total consistency: call requirements
      for(callReq in callReqs) {
        val callReqOutput = require(callReq.callee, cancel)
        logger.checkTaskReqStart(task, callReq)
        val reason = callReq.checkConsistency(callReqOutput)
        logger.checkTaskReqEnd(task, callReq, reason)
        if(reason != null) {
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          logger.requireTopDownEnd(task, execOutput)
          return execOutput
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validatePostWrite(task, data, it) }
      // Cache and mark as visited
      cache[task] = data
      visited[task] = data
      // Reuse existing result
      logger.requireTopDownEnd(task, existingOutput)
      return existingOutput
    } finally {
      layer.requireTopDownEnd(task)
    }
  }

  internal open fun <I : In, O : Out> exec(app: Task<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UTaskData {
    return shared.exec(app, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun <I : In, O : Out> execInternal(app: Task<I, O>, cancel: Cancelled): UTaskData {
    return shared.execInternal(app, cancel, this, this) { _, _ -> }
  }
}
