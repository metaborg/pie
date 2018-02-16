package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.AnyObsFuncApp
import mb.pie.runtime.core.exec.DirtyFlaggingTopDownExecutor
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


class DirtyFlaggingTopDownExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UFunc>,
  mbLogger: mb.log.Logger
) : DirtyFlaggingTopDownExecutor {
  private val mbLogger = mbLogger.forContext(DirtyFlaggingTopDownExecutorImpl::class.java)
  private val obsFuncApps = ConcurrentHashMap<Any, AnyObsFuncApp>()
  private val changedPaths = HashSet<PPath>()
  private val lock = ReentrantReadWriteLock()


  override fun add(key: Any, obsFuncApp: AnyObsFuncApp) {
    obsFuncApps[key] = obsFuncApp
  }

  override fun update(key: Any, obsFuncApp: AnyObsFuncApp) {
    obsFuncApps[key] = obsFuncApp
  }

  override fun remove(key: Any) {
    obsFuncApps.remove(key)
  }


  override fun pathChanged(path: PPath) {
    changedPaths.add(path)
  }

  override fun pathsChanged(paths: Collection<PPath>) {
    changedPaths.addAll(paths)
  }

  override fun dirtyFlag() {
    lock.write {
      try {
        val logger = logger.get()
        store.writeTxn().use { txn ->
          dirtyFlaggingAndPropagation(changedPaths, txn, logger)
        }
      } finally {
        changedPaths.clear()
        store.sync()
      }
    }
  }


  override fun executeAll(cancel: Cancelled) {
    lock.read {
      try {
        mbLogger.trace("Execution")
        val exec = DirtyFlaggingTopDownExec(store, cache, share, layer.get(), logger.get(), funcs)
        // TODO: observable functions may change during iteration, and will not be updated. Is this a problem?
        for((funcApp, changedFunc) in obsFuncApps.values) {
          mbLogger.trace("  requiring: ${funcApp.toShortString(200)}")
          val res = exec.require(funcApp, cancel)
          changedFunc(res.output)
        }
      } finally {
        store.sync()
      }
    }
  }

  override fun addAndExecute(key: Any, obsFuncApp: AnyObsFuncApp, cancel: Cancelled) {
    obsFuncApps[key] = obsFuncApp
    execOne(obsFuncApp, cancel)
  }

  override fun updateAndExecute(key: Any, obsFuncApp: AnyObsFuncApp, cancel: Cancelled) {
    obsFuncApps[key] = obsFuncApp
    execOne(obsFuncApp, cancel)
  }

  private fun execOne(obsFuncApp: AnyObsFuncApp, cancel: Cancelled) {
    lock.read {
      val exec = exec()
      try {
        val (funcApp, changedFunc) = obsFuncApp
        val res = exec.require(funcApp, cancel)
        changedFunc(res.output)
      } finally {
        store.sync()
      }
    }
  }


  fun exec(): DirtyFlaggingTopDownExec {
    return DirtyFlaggingTopDownExec(store, cache, share, layer.get(), logger.get(), funcs)
  }

  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}


open class DirtyFlaggingTopDownExec(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>
) : Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UFuncAppData>()
  private val generatorOfLocal = mutableMapOf<PPath, UFuncApp>()
  private val shared = TopDownExecShared(store, cache, share, layer, logger, visited, generatorOfLocal)


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
        val res = exec(app, NoResultReason(), cancel, true)
        logger.requireTopDownEnd(app, res)
        return res
      }
      val (output, _, pathReqs, pathGens) = data

      // Check for inconsistencies and re-execute when found.
      run {
        // Internal consistency: transient output consistency
        val reason = output.isTransientInconsistent()
        if(reason != null) {
          val res = exec(app, reason, cancel)
          logger.requireTopDownEnd(app, res)
          return res
        }
      }

      // Internal consistency: dirty flagged.
      if(store.readTxn().use { it.dirty(app) }) {
        val info = exec(app, DirtyFlaggedReason(), cancel)
        logger.requireTopDownEnd(app, info)
        return info
      }

      // Internal consistency: path generates
      for(pathGen in pathGens) {
        logger.checkPathGenStart(app, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathGenEnd(app, pathGen, reason)
          val res = exec(app, reason, cancel)
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          logger.checkPathGenEnd(app, pathGen, null)
        }
      }

      // TODO: is checking path reqs necessary?
      // Internal consistency: path requirements
      for(pathReq in pathReqs) {
        logger.checkPathReqStart(app, pathReq)
        val reason = pathReq.checkConsistency()
        if(reason != null) {
          // If a required file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathReqEnd(app, pathReq, reason)
          val res = exec(app, reason, cancel)
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          logger.checkPathReqEnd(app, pathReq, null)
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validatePostWrite(app, data, this, it) }
      store.writeTxn().use { txn ->
        // Flag generated files dirty.
        // TODO: is this necessary? If this func app is not executed, its generated files cannot change?
        dirtyFlaggingAndPropagation(pathGens.map { it.path }, txn, logger)
        // Mark not dirty.
        txn.setDirty(app, false)
      }
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

  internal open fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): ExecRes<O> {
    return shared.exec(app, reason, cancel, useCache, this::execInternal)
  }

  internal open fun <I : In, O : Out> execInternal(app: FuncApp<I, O>, cancel: Cancelled): O {
    return shared.execInternal(app, cancel, this, this) { txn, data ->
      // Flag generated files dirty.
      dirtyFlaggingAndPropagation(data.pathGens.map { it.path }, txn, logger)
      // Mark not dirty.
      txn.setDirty(app, false)
    }
  }
}

