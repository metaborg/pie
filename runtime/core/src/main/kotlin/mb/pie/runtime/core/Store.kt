package mb.pie.runtime.core

import mb.pie.runtime.core.impl.*
import mb.vfs.path.PPath


interface Store : AutoCloseable {
  /**
   * Opens a read transaction. Transaction must be [closed][close] after usage to free up internal resources.
   */
  fun readTxn(): StoreReadTxn

  /**
   * Opens a write transaction. Transaction must be [closed][close] after usage to commit written data and to free up
   * internal resources.
   */
  fun writeTxn(): StoreWriteTxn

  /**
   * Force synchronization of in-memory data to persistent storage.
   */
  fun sync()
}

interface StoreTxn : AutoCloseable {
  /**
   * Closes the transaction. Commits written data and frees up internal resources. Failure to close a transaction may
   * cause memory leaks and written data to not be visible to other transactions.
   */
  override fun close()
}

interface StoreReadTxn : StoreTxn {
  /**
   * @return `true` if [app] is marked as dirty, `false` otherwise.
   */
  fun dirty(app: UFuncApp): Boolean

  /**
   * @return output of [app], or `null` when no output was stored.
   */
  fun output(app: UFuncApp): Out?


  /**
   * @return call requirements of [app].
   */
  fun callReqs(app: UFuncApp): List<CallReq>

  /**
   * @return function applications that call [app].
   */
  fun callersOf(app: UFuncApp): Set<UFuncApp>


  /**
   * @return path requirements of [app].
   */
  fun pathReqs(app: UFuncApp): List<PathReq>

  /**
   * @return function applications that require [path].
   */
  fun requireesOf(path: PPath): Set<UFuncApp>


  /**
   * @return path generates of [app].
   */
  fun pathGens(app: UFuncApp): List<PathGen>

  /**
   * @return function application that generates [path], or `null` if it does not exist.
   */
  fun generatorOf(path: PPath): UFuncApp?


  /**
   * @return output, call requirements, path reqs, and path generates for [app], or `null` when no output was stored.
   */
  fun data(app: UFuncApp): UFuncAppData?
}

interface StoreWriteTxn : StoreReadTxn {
  /**
   * Marks [app] as dirty when [isDirty] is `true`, or as not-dirty when [isDirty] is `false`.
   */
  fun setDirty(app: UFuncApp, isDirty: Boolean)

  /**
   * Sets the output of [app] to [output].
   */
  fun setOutput(app: UFuncApp, output: Out)

  /**
   * Sets the call requirements of [app] to [callReqs].
   */
  fun setCallReqs(app: UFuncApp, callReqs: ArrayList<CallReq>)

  /**
   * Sets the path requirements of [app] to [pathReqs].
   */
  fun setPathReqs(app: UFuncApp, pathReqs: ArrayList<PathReq>)

  /**
   * Sets the generated pathGens of [app] to [pathGens].
   */
  fun setPathGens(app: UFuncApp, pathGens: ArrayList<PathGen>)

  /**
   * Sets the output, call requirements, path reqs, and path generates for [app] to [data].
   */
  fun setData(app: UFuncApp, data: UFuncAppData)

  /**
   * Removes all data from (drops) the store.
   */
  fun drop()
}


data class FuncAppData<out O : Out>(val output: O, val callReqs: ArrayList<CallReq>, val pathReqs: ArrayList<PathReq>, val pathGens: ArrayList<PathGen>)
typealias UFuncAppData = FuncAppData<*>

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
internal inline fun <O : Out> UFuncAppData.cast() = this as FuncAppData<O>