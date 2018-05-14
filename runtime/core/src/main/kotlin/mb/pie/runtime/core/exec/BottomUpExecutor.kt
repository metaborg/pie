package mb.pie.runtime.core.exec

import mb.pie.runtime.core.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
import mb.vfs.path.PPath


interface BottomUpExecutorFactory {
  fun create(store: Store, cache: Cache): BottomUpExecutor
}

interface BottomUpExecutor : Executor {
  @Throws(ExecException::class, InterruptedException::class)
  fun <I : In, O : Out> requireTopDown(task: Task<I, O>, cancel: Cancelled = NullCancelled()): O

  @Throws(ExecException::class, InterruptedException::class)
  fun requireBottomUp(changedFiles: Set<PPath>, cancel: Cancelled = NullCancelled())


  fun <I : In, O : Out> hasBeenRequired(task: Task<I, O>): Boolean


  fun setObserver(key: Any, task: UTask, observer: TaskObserver)

  fun removeObserver(key: Any)
}

typealias TaskObserver = (Out) -> Unit
