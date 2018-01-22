package mb.pie.runtime.core

import mb.vfs.path.PPath
import java.io.Closeable


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
  fun isDirty(app: UFuncApp): Boolean

  /**
   * @return execution result of [app], or `null` if no result was stored.
   */
  fun resultOf(app: UFuncApp): UExecRes?

  /**
   * @return function applications that are callers of [callee].
   */
  fun callersOf(callee: UFuncApp): Set<UFuncApp>

  /**
   * @return function applications that are a requiree of [path].
   */
  fun requireesOf(path: PPath): Set<UFuncApp>

  /**
   * @return function application that is a generator of [path], or `null` if no function applications generate [path].
   */
  fun generatorOf(path: PPath): UFuncApp?
}

interface StoreWriteTxn : StoreReadTxn {
  /**
   * Marks [app] as dirty when [isDirty] is `true`, or as not-dirty when [isDirty] is `false`.
   */
  fun setIsDirty(app: UFuncApp, isDirty: Boolean)

  /**
   * Sets the result of [app] to [result].
   */
  fun setResultOf(app: UFuncApp, result: UExecRes)

  /**
   * Sets [caller] as a caller of [callee].
   */
  fun setCallerOf(caller: UFuncApp, callee: UFuncApp)

  /**
   * Sets [requiree] as a requiree of [path].
   */
  fun setRequireeOf(requiree: UFuncApp, path: PPath)

  /**
   * Sets [generator] as a generator of [path].
   */
  fun setGeneratorOf(generator: UFuncApp, path: PPath)

  /**
   * Removes all data from (drops) the store.
   */
  fun drop()
}
