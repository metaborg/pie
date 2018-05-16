package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.util.async.Cancelled

class TopDownExecutorImpl constructor(
  private val taskDefs: TaskDefs,
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val layerFactory: (Logger) -> Layer,
  private val logger: Logger,
  private val executorLoggerFactory: (Logger) -> ExecutorLogger
) : TopDownExecutor {
  override fun newSession(): TopDownSession {
    return TopDownSessionImpl(taskDefs, store, cache, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layerFactory(logger), logger, executorLoggerFactory(logger))
  }
}

open class TopDownSessionImpl(
  taskDefs: TaskDefs,
  private val store: Store,
  private val cache: Cache,
  share: Share,
  defaultOutputStamper: OutputStamper,
  defaultFileReqStamper: FileStamper,
  defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  logger: Logger,
  private val executorLogger: ExecutorLogger
) : TopDownSession, RequireTask {
  private val visited = mutableMapOf<UTask, UTaskData>()
  private val shared = TopDownExecShared(taskDefs, visited, store, cache, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layer, logger, executorLogger)


  override fun <I : In, O : Out> requireInitial(task: Task<I, O>, cancel: Cancelled): O {
    try {
      executorLogger.requireTopDownInitialStart(task)
      val info = require(task, cancel)
      executorLogger.requireTopDownInitialEnd(task, info)
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
        executorLogger.requireTopDownEnd(task, output)
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
          executorLogger.requireTopDownEnd(task, execOutput)
          return execOutput
        }
      }

      // Internal consistency: file requirements
      for(pathReq in pathReqs) {
        executorLogger.checkFileReqStart(task, pathReq)
        val reason = pathReq.checkConsistency()
        if(reason != null) {
          // If a required file is outdated (i.e., its stamp changed): rebuild
          executorLogger.checkFileReqEnd(task, pathReq, reason)
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(task, execOutput)
          return execOutput
        } else {
          executorLogger.checkFileReqEnd(task, pathReq, null)
        }
      }

      // Internal consistency: file generates
      for(pathGen in pathGens) {
        executorLogger.checkFileGenStart(task, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          executorLogger.checkFileGenEnd(task, pathGen, reason)
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(task, execOutput)
          return execOutput
        } else {
          executorLogger.checkFileGenEnd(task, pathGen, null)
        }
      }

      // Total consistency: call requirements
      for(callReq in callReqs) {
        val callReqOutput = require(callReq.callee, cancel)
        executorLogger.checkTaskReqStart(task, callReq)
        val reason = callReq.checkConsistency(callReqOutput)
        executorLogger.checkTaskReqEnd(task, callReq, reason)
        if(reason != null) {
          val execData = exec(task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(task, execOutput)
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
      executorLogger.requireTopDownEnd(task, existingOutput)
      return existingOutput
    } finally {
      layer.requireTopDownEnd(task)
    }
  }

  internal open fun <I : In, O : Out> exec(app: Task<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UTaskData {
    return shared.exec(app, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun <I : In, O : Out> execInternal(app: Task<I, O>, cancel: Cancelled): UTaskData {
    return shared.execInternal(app, cancel, this) { _, _ -> }
  }
}
